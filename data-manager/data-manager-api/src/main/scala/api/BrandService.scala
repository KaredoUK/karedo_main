package api



import java.util.UUID

import akka.actor.{ActorRef}
import akka.event.slf4j.Logger
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}
import core.BrandActor.{InternalBrandError, BrandError}
import core.EditAccountActor.{ListBrandsRequest, EditAccountError, AddBrand}
import core.{SuccessResponse, ResponseWithFailure}
import parallelai.wallet.entity.{AdvertisementDetail, SuggestedAdForUsersAndBrandModel}



import spray.routing.Directives

import scala.concurrent.ExecutionContext


object BrandService {
  val logger = Logger("BrandService")
}

class BrandService(brandActor: ActorRef, editAccountActor: ActorRef)(implicit executionContext: ExecutionContext)
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

  val routesuggestedBrands =
    path("account" / JavaUUID / "brand" / JavaUUID / "ads" ) { (accountId: UUID, brandId: UUID) =>
      get {
        parameters('max.as[Int]) { max =>
          rejectEmptyResponse {
            complete {
              (brandActor ? RequestSuggestedAdForUsersAndBrand(accountId,brandId,max)).
                mapTo[List[SuggestedAdForUsersAndBrand]]

            }
          }
        }
      }
    }

  val routesuggestedBrandsDummy =
    path("account" / JavaUUID / "suggestedBrands") { accountId: UserID =>
      post {
        handleWith {
          brandIdRequest: BrandIDRequest =>
            (editAccountActor ? AddBrand(accountId, brandIdRequest.brandId)).mapTo[ResponseWithFailure[EditAccountError, String]]
        }
      } ~ get {
        complete {
          (editAccountActor ? ListBrandsRequest(accountId)).mapTo[ResponseWithFailure[EditAccountError, List[BrandRecord]]]
        }
      }
    }

  val route = routebrand ~ routebrandWithId ~
    routebrandWithIdAdvert ~ routebrandWithIdAdvertWithId ~ /* routesuggestedBrands ~ */ routesuggestedBrandsDummy
}
