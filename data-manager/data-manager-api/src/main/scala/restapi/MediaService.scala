package restapi

import java.io.ByteArrayInputStream
import java.util.UUID

import akka.actor.ActorRef

import akka.event.slf4j.Logger
import akka.util.Timeout
import restapi.MediaService.logger
import com.parallelai.wallet.datamanager.data._

import core.MediaContentActor._
import core.{FailureResponse, SuccessResponse, ResponseWithFailure}
import spray.http._
import spray.routing.Directives

import scala.Some
import scala.concurrent.{Await, ExecutionContext}
import core.MediaContentActor.{InvalidContentId, MediaHandlingError}
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol.addMediaResponseJson
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol.getMediaResponseJson
import scala.util.{Success, Failure}
import scala.concurrent.Future



object MediaService {
  val logger = Logger("MediaService")
}

class MediaService(mediaActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol  {

  import akka.pattern.ask

import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val routeput =
    path("media") {
      post {
        headerValueByName(HttpHeaders.`Content-Type`.name) { contentType =>
          entity(as[MultipartFormData]) { formData: MultipartFormData =>
              complete {


               formData.fields.head match {
                  case (BodyPart(entity, headers)) =>
                    val file_bin = entity.data.toByteArray

                    val contentType = headers.find(h => h.is("content-type")).get.value
                    val fileName = headers.find(h => h.is("content-disposition")).get.value.split("filename=").last


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


    val routeget = path("media" / Segment) { mediaId: String =>
      get {
        detach() {
          val mediaResponseFuture = (mediaActor ? GetMediaRequest(mediaId)).mapTo[ResponseWithFailure[MediaHandlingError, Option[GetMediaResponse]]]

          val mediaResponse = Await.result(mediaResponseFuture, timeout.duration)

          mediaResponse  match {
            case FailureResponse(mediaHandlingError) => respondWithStatus(mediaHandlingError) { complete { "" } } // Taking advandate of the ErrorSelector implicit converter

            case SuccessResponse(Some(GetMediaResponse(contentType, content))) =>

              val parts = contentType.split("/")
              require(parts.length == 2, s"Invalid Content type $contentType for media with ID $mediaId")

              respondWithMediaType(MediaTypes.getForKey((parts(0), parts(1))).get) {
                complete {
                  logger.info(s"Transmitting content of length ${content.length}")
                  HttpData(content)
                }
              }

            case SuccessResponse(None) => respondWithStatus(StatusCodes.NotFound) { complete { "" }}
          }
        }
      }
    }

  val route = routeget ~ routeput
}
