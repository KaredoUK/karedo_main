package api

import java.util.UUID

import akka.actor.ActorRef

import akka.event.slf4j.Logger
import akka.util.Timeout
import api.MediaService.logger
import com.parallelai.wallet.datamanager.data._

import core.MediaContentActor.MediaHandlingError
import core.{SuccessResponse, ResponseWithFailure}
import spray.http.{HttpEntity, MultipartFormData}
import spray.routing.Directives

import scala.concurrent.{Await, ExecutionContext}
import core.MediaContentActor.{InvalidContentId, MediaHandlingError}
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol.addMediaResponseJson
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol.getMediaResponseJson
import scala.util.{Success, Failure}

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

        entity(as[MultipartFormData]) { formData: MultipartFormData =>

          complete {

            formData.get("media") match {
              case Some(p) => {
                val file_entity: HttpEntity = p.entity
                val file_bin = file_entity.data.toByteArray

                logger.info(s"Found a file with ${file_bin.length} bytes")

                (mediaActor ? AddMediaRequest("media", "contenttype", file_bin))
                  .mapTo[ResponseWithFailure[MediaHandlingError, AddMediaResponse]]


              }
              case _ => ""

            }

          }
        }
      }
    }


    val routeget = path("media" / Segment) {

      mediaId: String =>
        rejectEmptyResponse {
          get {
            complete {

              (mediaActor ? GetMediaRequest(mediaId)).mapTo[ResponseWithFailure[MediaHandlingError, GetMediaResponse]]
            }
          }

        }
    }

  val route = routeget ~ routeput
}
