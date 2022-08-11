package restapi

import java.util.UUID

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
//import com.wordnik.swagger.annotations._
import core.EditAccountActor.{EditAccountError, _}
import core.OfferActor.OfferError
import core.RegistrationActor._
import core.ResponseWithFailure
import core.security.UserAuthService
import restapi.security.AuthorizationSupport
import spray.routing._

import scala.concurrent.ExecutionContext

abstract class AccountHttpService
(
  protected val registrationActor: ActorRef,
  protected val editAccountActor: ActorRef,
  protected val brandActor: ActorRef,
  protected val offerActor: ActorRef,
  override protected val userAuthService: UserAuthService)
(protected implicit val executionContext: ExecutionContext)

extends HttpService
  with Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with CORSDirectives
  with AuthorizationSupport {

  import scala.concurrent.duration._
  implicit val timeout = Timeout(20.seconds)

  def route =
    pathPrefix("account")  {

      create ~               // P77 POST /account
        validate ~           // P53 POST /account/validation/validate
        confirmActivation ~    // KAR XXX quick activation
        reset ~              // P49 PUT  /account/xxx/application/xxx/reset
        addApplication ~     // P101 POST /account/application
        login ~              // P102 POST /account/xxx/application/xxx/login
        getUserProfile ~     // P51 GET AUTH /account/xxx
        editUserProfile ~    // P50 PUT AUTH /account/xxx
        deleteUserProfile ~  // P52 DELETE AUTH /account/xxx
        getPoints ~          // P54 GET AUTH /account/xxx/points
        addBrandToUser ~     // P90 POST AUTH /account/xxx/brand
        removeUserBrand ~    // P71 DELETE AUTH /account/xxx/brand/xxx
        showUserBrands ~     // P69 GET AUTH /account/xxx/brand
        suggestedAdsForBrand ~ // P59 GET AUTH /account/xxx/brand/xxx
        suggestedBrandsPost ~ // P70 ???
        suggestedBrandsGet ~   // P70 GET AUTH /account/xxx/suggestedbrands
        P123 ~              // get number of ACTIVE ads for user/brand GET /account/xxxx/brand/yyyy
        P124                // get offers active for this account
    }
    
  
  // PARALLELAI-77API: Create Account

  def create = corsFilter(List("*")) {
    pathEnd {
      post {
        handleWith {

          registrationRequest: RegistrationRequest =>
            (registrationActor ? registrationRequest).
              mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
        }
      }
    }
  }



  // PARALLELAI-53API: Validate/Activate Account Application
  def validate = path("application" / "validation") {
    post {
      handleWith {
        registrationValidation: RegistrationValidation =>
          (registrationActor ? registrationValidation).
            mapTo[ResponseWithFailure[RegistrationError, RegistrationValidationResponse]]
      }
    }
  }

  def confirmActivation = path("confirmActivation" / JavaUUID / JavaUUID) {
    (deviceId: DeviceID, userId: UserID ) =>
    get {
      complete (
        (registrationActor ? RegistrationConfirmActivation(deviceId,userId)).
          mapTo[ResponseWithFailure[RegistrationError,RegistrationConfirmActivationResponse]]
      )
    }
  }

  // PARALLELAI-49
  def reset =
    path(JavaUUID / "application" / JavaUUID / "reset") {
      (accountId: UserID, deviceId: DeviceID) =>
        put {
          complete {
            (registrationActor ? AddApplicationToKnownUserRequest(deviceId, accountId)).
              mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
          }
        }
    }

  // PARALLELAI-52 delete userprofile
  def deleteUserProfile =
  path(JavaUUID) { accountId: UserID =>
    userAuthorizedFor(canAccessUser(accountId))(executionContext) {
      userAuthContext =>
        delete {
          complete {
            (editAccountActor ? DeleteAccount(accountId)).
              mapTo[ResponseWithFailure[EditAccountError, StatusResponse]]
          }
        }
    }
  }

  // PARALLELAI-102 API: User Login
  def login =
  // POST /account/$UserID/application/$ApplicationId/login {
    path(JavaUUID / "application" / JavaUUID / "login") {
      (accountId: UserID, deviceId: DeviceID) =>
        post {
          handleWith {
            loginRequest: APILoginRequest =>

              (registrationActor ? LoginRequest(accountId, deviceId, loginRequest.password)).
                mapTo[ResponseWithFailure[RegistrationError, APISessionResponse]]
          }
        }
    }




  // PARALLELAI-51 get user profile / account settings
  def getUserProfile =
    path(JavaUUID) { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) {
        userAuthContext =>
          rejectEmptyResponse {
            get {
              complete {
                (editAccountActor ? GetAccount(accountId)).
                  mapTo[ResponseWithFailure[EditAccountError, Option[UserProfile]]]
              }
            }
          }
      }
    }

  // PARALLELAI-50 update userprofile
  def editUserProfile =
    path(JavaUUID) { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) {
        userAuthContext =>

          put {
            handleWith {
              userProfile: UserProfile =>
                (editAccountActor ? UpdateAccount(userProfile)).
                  mapTo[ResponseWithFailure[EditAccountError,StatusResponse]]
            }
          }
      }
    }
  // PARALLELAI-54API: Get User Points
  def getPoints =
    path(JavaUUID / "points") { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) {
        userAuthContext =>
          rejectEmptyResponse {
            get {
              complete {
                (editAccountActor ? GetAccountPoints(accountId)).
                  mapTo[ResponseWithFailure[RegistrationError, Option[UserPoints]]]
              }
            }
          }
      }
    }

  // PARALLELAI-90API: Add Brand to User
  def addBrandToUser =
    path(JavaUUID / "brand") { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) {
        userAuthContext =>
          post {
            handleWith {
              brandIdRequest: BrandIDRequest =>
                (editAccountActor ? AddBrand(accountId, brandIdRequest.brandId)).
                  mapTo[ResponseWithFailure[EditAccountError, StatusResponse]]
            }
          }
      }
    }

  // title("PARALLELAI-71API: Remove User Brand")
  // r = delete("account/"+userId+"/brand/"+brandId)
  def removeUserBrand : Route =
    path(JavaUUID / "brand" / JavaUUID) {
      (user, brand) =>
      {
        userAuthorizedFor(canAccessUser(user))(executionContext) { userAuthContext =>
          delete {
            complete {
              (editAccountActor ? RemoveBrand(user, brand)).
                mapTo[ResponseWithFailure[EditAccountError, StatusResponse]]
            }
          }
        }
      }
    }

  // PARALLELAI-69API: Show User Brands
  def showUserBrands =
    path(JavaUUID / "brand") { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) {
        userAuthContext =>
          get {
            complete {
              (editAccountActor ? ListBrandsRequest(accountId)).
                mapTo[ResponseWithFailure[EditAccountError, List[BrandRecord]]]
            }
          }
      }

    }

  // PARALLELAI-101: Add Application to Existing User
  def addApplication =
    path("application") {
      post {
        handleWith {
          addApplicationToUserRequest: AddApplicationRequest =>
            (registrationActor ? addApplicationToUserRequest).
              mapTo[ResponseWithFailure[RegistrationError, AddApplicationResponse]]
        }
      }
    }

  def suggestedAdsForBrand =
    path(JavaUUID / "brand" / JavaUUID / "ads") { (accountId: UUID, brandId: UUID) =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) { userAuthContext =>
        get {
          parameters('max.as[Int]) { max =>
            rejectEmptyResponse {
              complete {
                val ret = (brandActor ? RequestSuggestedAdForUsersAndBrand(accountId, brandId, max)).
                mapTo[List[SuggestedAdForUsersAndBrand]]
                ret
                
              }
            }
          }
        }
      }
    }

  def suggestedBrandsPost =
    path(JavaUUID / "suggestedBrands") { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) { userAuthContext =>
        post {
          handleWith {
            brandIdRequest: BrandIDRequest =>
              (editAccountActor ?
                AddBrand(accountId, brandIdRequest.brandId)).

                mapTo[ResponseWithFailure[EditAccountError, StatusResponse]]
          }
        }
      }
    }
  def suggestedBrandsGet =
    path(JavaUUID / "suggestedBrands") { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) { userAuthContext =>
        get {
          complete {
            (editAccountActor ? ListBrandsRequest(accountId)).mapTo[ResponseWithFailure[EditAccountError, List[BrandRecord]]]
          }
        }
      }
    }

  // GET /account/xxxx/brand/yyyy
  def P123 =
    path(JavaUUID / "brand" / JavaUUID ){
      (accountId: UserID, brandId: UUID) =>
        userAuthorizedFor(canAccessUser(accountId))(executionContext) { userAuthContext =>
          get {
            complete {
              (editAccountActor ? GetActiveAccountBrandOffers(accountId, brandId)).
                mapTo[ResponseWithFailure[EditAccountError, GetActiveAccountBrandOffersResponse]]

            }
          }
        }
    }

  def P124 =
    path(JavaUUID / "acceptedoffers" ){
      (accountId: UserID) =>
        userAuthorizedFor(canAccessUser(accountId))(executionContext) { userAuthContext =>
          get {
            complete {
              (offerActor ? GetAcceptedOffers(accountId)).
                mapTo[ResponseWithFailure[OfferError, List[KaredoSalesApi]]]

            }
          }
        }
    }

}
