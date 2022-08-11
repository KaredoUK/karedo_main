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


object MediaHttpService {
  val logger = Logger("MediaService")
}


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

  def routeget = path("media" / Segment) { mediaId: String =>
    //userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
      get {
        detach() {
          val mediaResponseFuture = (mediaActor ? GetMediaRequest(mediaId)).mapTo[ResponseWithFailure[MediaHandlingError, Option[GetMediaResponse]]]

          val mediaResponse = Await.result(mediaResponseFuture, timeout.duration)

          mediaResponse match {
            case FailureResponse(mediaHandlingError) => respondWithStatus(mediaHandlingError) { complete { "" } } // Taking advandage of the ErrorSelector implicit converter

            case SuccessResponse(Some(GetMediaResponse(contentType, content))) =>
              logger.info(s"contentType is $contentType")
              val parts = contentType.split("/")
              if(parts.length != 2) {
                complete(s"Invalid contentType $contentType")
              } else {

                logger.info("trying to send it")
                val (p1,p2)=(parts(0),parts(1))
                val mediaType = MediaTypes.getForKey((p1, p2)).get

                respondWithMediaType(mediaType) {
                  logger.info("trying to respond with mediatype")
                  complete {

                    logger.info(s"Transmitting content of length ${content.length}")
                    HttpData(content)
                  }
                }
              }

            case SuccessResponse(None) => respondWithStatus(StatusCodes.NotFound) { complete { "KO" } }
          }
        }
      }
    //}
  }

  val route = routeget ~ routeput
}
