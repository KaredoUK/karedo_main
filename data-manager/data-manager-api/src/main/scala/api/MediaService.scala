package api

import java.util.UUID

import akka.actor.ActorRef

import akka.event.slf4j.Logger
import akka.util.Timeout
import api.MediaService.logger
import com.parallelai.wallet.datamanager.data._

import core.MediaContentActor._
import core.{FailureResponse, SuccessResponse, ResponseWithFailure}
import spray.http._
import spray.routing.Directives

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
              formData.get("media") match {
                case Some(p) =>
                  val file_entity: HttpEntity = p.entity
                  val file_bin = file_entity.data.toByteArray

                  logger.info(s"Found a file with ${file_bin.length} bytes")

                  (mediaActor ? AddMediaRequest("media", contentType, file_bin))
                    .mapTo[ResponseWithFailure[MediaHandlingError, AddMediaResponse]]

                case None => Future.successful(FailureResponse[MediaHandlingError, AddMediaResponse](MissingContent))

              }
            }
          }
        }
      }
    }

    val routeget = path("media" / Segment) { mediaId: String =>
      get {
        detach() {
          val mediaResponseFuture = (mediaActor ? GetMediaRequest(mediaId)).mapTo[ResponseWithFailure[MediaHandlingError, GetMediaResponse]]

          val mediaResponse = Await.result(mediaResponseFuture, timeout.duration)

          mediaResponse  match {
            case FailureResponse(mediaHandlingError) => respondWithStatus(mediaHandlingError) { complete { "" } } // Taking advandate of the ErrorSelector implicit converter

            case SuccessResponse(GetMediaResponse(contentType, content)) =>

              val parts = contentType.split("/")
              require(parts.length == 2, s"Invalid Content type $contentType for media with ID $mediaId")

              respondWithMediaType(MediaTypes.getForKey((parts(0), parts(1))).get) {
                complete { HttpData(content) }
              }
          }
        }
      }
    }

  val route = routeget ~ routeput
}
