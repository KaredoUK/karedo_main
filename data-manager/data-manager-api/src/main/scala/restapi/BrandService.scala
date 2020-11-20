package restapi



import java.util.UUID

import akka.actor.{ActorRef}
import akka.event.slf4j.Logger
import akka.util.Timeout
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse, ListBrandsAdverts, _}

import core.EditAccountActor.{ListBrandsRequest, EditAccountError, AddBrand}
import core.security.UserAuthService
import core.{SuccessResponse, ResponseWithFailure}
import core.objAPI._
import parallelai.wallet.entity.{UserAuthContext, AdvertisementDetail, SuggestedAdForUsersAndBrandModel}


import spray.routing._

import scala.concurrent.ExecutionContext


object BrandService {
  val logger = Logger("BrandService")
}

class BrandService(brandActor: ActorRef, editAccountActor: ActorRef,
                  override protected val userAuthService: UserAuthService)
                  (implicit protected val executionContext: ExecutionContext)
  extends Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with AuthorizationSupport
{


  import akka.pattern.ask

  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route55: Route =

  // PARALLELAI-55API: User Brand Interaction
  // "user/"+userId+"/interaction/brand/"+brandId, { "interactionType":  "BUY"}
    path("user" / JavaUUID / "interaction" / "brand" / JavaUUID) {
      (user, brand) => {
        userAuthorizedFor( canAccessUser(user) )(executionContext) { userAuthContext =>
          post {
            handleWith((intType: String) =>

              (brandActor ? UserBrandInteraction(user, brand, intType)).mapTo[ResponseWithFailure[APIError, InteractionResponse]]
              // s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}")
            )
          }
        }
      }

    }

  // dealing with /brand to create inquiry a brand
  val routebrand_67_95 =
    path("brand") {
      userAuthorizedFor( isLoggedInUser )(executionContext) { userAuthContext =>

      post {
          handleWith {
            brandData: BrandData =>
              (brandActor ? brandData).mapTo[ResponseWithFailure[APIError, BrandResponse]]
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
    }

  val routebrandWithId =

    path("brand" / JavaUUID) { brandId: UUID =>
      userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>
        rejectEmptyResponse {
          get {
            complete {

              (brandActor ? BrandIDRequest(brandId)).mapTo[ResponseWithFailure[APIError, BrandRecord]]
            }
          }

        } ~ delete {
          complete {
            (brandActor ? DeleteBrandRequest(brandId)).mapTo[ResponseWithFailure[APIError, String]]
          }
        }
      }
    }

  val routebrandWithIdAdvert =

    path("brand" / JavaUUID / "advert") {

      brandId: UUID =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>

          rejectEmptyResponse {
            get {
              complete {

                (brandActor ? ListBrandsAdverts(brandId)).mapTo[ResponseWithFailure[APIError, List[AdvertDetailResponse]]]

              }
            }
          } ~ post {
            handleWith {
              request: AdvertDetail => {
                (brandActor ? AddAdvertCommand(brandId, request.text, request.imageIds, request.value)).mapTo[ResponseWithFailure[APIError, AdvertDetailResponse]]
              }
            }
          }
        }
    }

  val routebrandWithIdAdvertWithId =

    path("brand" / JavaUUID / "advert" / JavaUUID) {
      (brandId: UUID, advId: UUID) =>
        userAuthorizedFor(isLoggedInUser)(executionContext) { userAuthContext =>

          delete {
            complete {
              (brandActor ? DeleteAdvRequest(brandId, advId)).mapTo[ResponseWithFailure[APIError, String]]


            }
          }
        }
    }

  val routesuggestedBrands =
    path("account" / JavaUUID / "brand" / JavaUUID / "ads" ) { (accountId: UUID, brandId: UUID) =>
      userAuthorizedFor( canAccessUser(accountId) )(executionContext) { userAuthContext =>
        get {
          parameters('max.as[Int]) { max =>
            rejectEmptyResponse {
              complete {
                (brandActor ? RequestSuggestedAdForUsersAndBrand(accountId, brandId, max)).
                  mapTo[List[SuggestedAdForUsersAndBrand]]

              }
            }
          }
        }
      }
    }

  val routesuggestedBrandsDummy =
    path("account" / JavaUUID / "suggestedBrands") { accountId: UserID =>
      userAuthorizedFor( canAccessUser(accountId) )(executionContext) { userAuthContext =>
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
    }

  val route = route55 ~ routebrand_67_95 ~ routebrandWithId ~
    routebrandWithIdAdvert ~ routebrandWithIdAdvertWithId ~ /* routesuggestedBrands ~ */ routesuggestedBrandsDummy
}
