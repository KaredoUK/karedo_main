package restapi

import java.io.ByteArrayInputStream
import java.util.UUID

import akka.actor.ActorRef

import akka.event.slf4j.Logger
import akka.util.Timeout
import restapi.MediaHttpService.logger
import com.parallelai.wallet.datamanager.data._

import core.MediaContentActor._
import core.{FailureResponse, SuccessResponse, ResponseWithFailure}

import restapi.security.AuthorizationSupport
import core.security.UserAuthService

import spray.http._
import spray.routing.{HttpService, Directives}

import scala.Some
import scala.concurrent.{Await, ExecutionContext}
import core.MediaContentActor.{InvalidContentId, MediaHandlingError}
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol.addMediaResponseJson
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol.getMediaResponseJson
import scala.util.{Success, Failure}
import scala.concurrent.Future
import com.wordnik.swagger.annotations.{Api => ApiDoc, _}


object MediaHttpService {
  val logger = Logger("MediaService")
}

@ApiDoc(value = "/media", description = "Media Manager, creates and retrieve media content.", position = 0)
abstract class MediaHttpService (mediaActor: ActorRef,
    override protected val userAuthService: UserAuthService)
    (implicit executionContext: ExecutionContext)
  extends HttpService
  with Directives
  with DefaultJsonFormats 
  with ApiErrorsJsonProtocol
  with AuthorizationSupport {

  import akka.pattern.ask

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  @ApiOperation(httpMethod = "POST", response = classOf[AddMediaResponse],
    value = "PARALLELAI-94: API: Upload Media File")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "content", required = true, dataType = "Array[Byte]",  paramType = "body",
      value = "Media content"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Content")
  ))
  def routeput =
    path("media") {
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        {
          post {
            headerValueByName("X-Content-Type") { contentType =>
              entity(as[MultipartFormData]) { formData: MultipartFormData =>
                complete {

                  formData.fields.head match {
                    case (BodyPart(entity, headers)) =>
                      val file_bin = entity.data.toByteArray

                      val fileName = headers.find(h => h.is("content-disposition")) match {
                        case Some(header) =>
                          header.value.split("filename=").last
                        case None => "unknown"
                      }


                      logger.info(s"Found a media of type '${contentType}' named '${fileName}' with '${file_bin.length}' bytes")

                      (mediaActor ? AddMediaRequest(fileName, contentType, file_bin))
                        .mapTo[ResponseWithFailure[MediaHandlingError, AddMediaResponse]]

                    case _ => Future.successful[MediaHandlingError](InvalidContentId(new Exception("Missing bodypart")))
                  }

                }
              }
            }
          }
        }
      }
    }

  @ApiOperation(httpMethod = "GET", response = classOf[Array[Byte]],
    value = "PARALLELAI-97: API: Retrieve Media File")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "mediaId", required = true, dataType = "String", paramType = "path",
      value = "The ID of the media to retrieve"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 404, message = "Not found")
  ))
  def routeget = path("media" / Segment) { mediaId: String =>
    userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
      get {
        detach() {
          val mediaResponseFuture = (mediaActor ? GetMediaRequest(mediaId)).mapTo[ResponseWithFailure[MediaHandlingError, Option[GetMediaResponse]]]

          val mediaResponse = Await.result(mediaResponseFuture, timeout.duration)

          mediaResponse match {
            case FailureResponse(mediaHandlingError) => respondWithStatus(mediaHandlingError) { complete { "" } } // Taking advandage of the ErrorSelector implicit converter

            case SuccessResponse(Some(GetMediaResponse(contentType, content))) =>

              val parts = contentType.split("/")
              require(parts.length == 2, s"Invalid Content type $contentType for media with ID $mediaId")

              respondWithMediaType(MediaTypes.getForKey((parts(0), parts(1))).get) {
                complete {
                  logger.info(s"Transmitting content of length ${content.length}")
                  HttpData(content)
                }
              }

            case SuccessResponse(None) => respondWithStatus(StatusCodes.NotFound) { complete { "" } }
          }
        }
      }
    }
  }

  val route = routeget ~ routeput
}
