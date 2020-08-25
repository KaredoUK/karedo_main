package api


import java.util.UUID

import akka.actor.ActorRef
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}
import core.BrandActor.BrandError
import core.ResponseWithFailure

import spray.routing.Directives

import scala.concurrent.ExecutionContext


class BrandService(brandActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {


  import akka.pattern.ask

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)


  val route1 =
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

  val route2 =

    path("brand" / JavaUUID) { brandId: UUID =>
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

  val route3 =

    path("brand" / JavaUUID / "advert") { brandId: UUID =>
      rejectEmptyResponse {
        get {
          complete {
            (brandActor ? ListBrandsAdverts(brandId)).mapTo[ResponseWithFailure[BrandError, List[AdvertisementDetailResponse]]]
          }
        }
      }
    }

  val route = route1 ~ route2 ~ route3
}
