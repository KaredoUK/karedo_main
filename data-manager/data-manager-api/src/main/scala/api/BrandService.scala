package api


import java.io.ByteArrayInputStream
import java.util.UUID

import akka.actor.{ActorLogging, ActorRef}
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}
import core.BrandActor.{InternalBrandError, BrandError}
import core.{FailureResponse, SuccessResponse, ResponseWithFailure}
import org.slf4j.LoggerFactory
import spray.http.{HttpEntity, BodyPart, MultipartFormData}

import spray.routing.Directives
import spray.util.{SprayActorLogging, LoggingContext}

import scala.concurrent.ExecutionContext

import api.BrandService.logger
object BrandService {
  val logger = Logger("BrandService")
}

class BrandService(brandActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol  {


  import akka.pattern.ask

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)


  val routebrand =
    path("brand") {


      post {
        handleWith {
          brandData: BrandData =>
            (brandActor ? brandData).mapTo[ResponseWithFailure[BrandError, BrandResponse]]
        }
      } ~
        get {
          rejectEmptyResponse {

            complete {


              (brandActor ? ListBrands).mapTo[List[BrandRecord]]
            }
          }
        }

    }

  val routebrandWithId =

    path("brand" / JavaUUID) {

      brandId: UUID =>
      rejectEmptyResponse {
        get {
          complete {

            (brandActor ? BrandIDRequest(brandId)).mapTo[ResponseWithFailure[BrandError, BrandRecord]]
          }
        }

      } ~ delete {
        complete {
          (brandActor ? DeleteBrandRequest(brandId)).mapTo[ResponseWithFailure[BrandError, String]]
        }
      }
    }

  val routebrandWithIdAdvert =

    path("brand" / JavaUUID / "advert") {

      brandId: UUID =>
      rejectEmptyResponse {
        get {
          complete {

            (brandActor ? ListBrandsAdverts(brandId)).mapTo[ResponseWithFailure[BrandError, List[AdvertDetailResponse]]]
          }
        }
      } ~ post {
        handleWith {
          request: AdvertDetail => {
            (brandActor ? AddAdvertCommand(brandId, request.text, request.imageIds, request.value)).mapTo[ResponseWithFailure[BrandError, AdvertDetailResponse]]
          }
        }
      }
    }

  val routebrandWithIdAdvertWithId =

    path("brand" / JavaUUID / "advert" / JavaUUID) {

      (brandId: UUID, advId: UUID) =>
        delete {
          complete {
          (brandActor ? DeleteAdvRequest (brandId, advId) ).mapTo[ResponseWithFailure[BrandError, String]]


          }
        }
    }

  val routeMedia =
    (path("media") & post) {
      entity(as[MultipartFormData]) { formData: MultipartFormData =>

        complete {

          formData.get("media") match {
            case Some(p) => {
              val file_entity: HttpEntity = p.entity
              val file_bin = file_entity.data.toByteArray

              logger.info(s"Found a file with ${file_bin.length} bytes")

              (brandActor ? AddMediaRequest("media", "contenttype", file_bin)).mapTo[ResponseWithFailure[BrandError, AddMediaResponse]]

            }
            case _ => ""

          }

        }
      }
    }


  val route = routebrand ~ routebrandWithId ~ routebrandWithIdAdvert ~ routebrandWithIdAdvertWithId ~ routeMedia
}
