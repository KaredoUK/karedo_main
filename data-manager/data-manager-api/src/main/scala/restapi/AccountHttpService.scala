package restapi

import java.util.UUID
import javax.ws.rs.Path

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.parallelai.wallet.datamanager.data._
import com.wordnik.swagger.annotations._
import core.EditAccountActor.{EditAccountError, _}
import core.OfferActor.OfferError
import core.RegistrationActor._
import core.ResponseWithFailure
import core.security.UserAuthService
import restapi.security.AuthorizationSupport
import spray.routing._

import scala.concurrent.ExecutionContext

// All APIs starting with /account go here
@Api(position=1, value = "/account", description = "Operations on the account")
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

  @ApiOperation(position= 1,httpMethod = "POST", response = classOf[RegistrationResponse], value = "Parallelai-77: Create a new Account")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "request", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.RegistrationRequest", paramType = "body",
      value = "Details of the request")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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
  @Path("/application/validation")
  @ApiOperation(position=2,httpMethod = "POST", response = classOf[RegistrationValidationResponse],
    value = "Parallelai-53: Validates Registration")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "request",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.RegistrationValidation",
      paramType = "body",
      value = "Details to be validated")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Wrong validation code")
  ))
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
  @Path("/{account}/application/{application}/reset")
  @ApiOperation(position=3,httpMethod = "PUT", response = classOf[RegistrationResponse],
    value = "Parallelai-49: Reset an application for a user, allowing them to register again")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user account to be reset"),
    new ApiImplicitParam(name = "application", required = true, dataType = "String", paramType = "path",
      value = "UUID of the application to be reset")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Wrong validation code")
  ))
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

  @Path("/{account}/application/{application}/login")
  @ApiOperation(position=4,httpMethod = "POST", response = classOf[APISessionResponse],
    value = "Parallelai-102: Perform a login specifying password")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user account trying to login"),
    new ApiImplicitParam(name = "application", required = true, dataType = "String", paramType = "path",
      value = "UUID of the application from where user is trying to login"),
    new ApiImplicitParam(
      name = "login request",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.APILoginRequest",
      paramType = "body",
      value = "Login Credentials (password)")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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

  @Path("/{account}")
  @ApiOperation(position=5,httpMethod = "GET", response = classOf[UserProfile],
    value = "Parallelai-51: Get information about the user")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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




  @Path("/{account}")
  @ApiOperation(position=6,httpMethod = "PUT", response = classOf[StatusResponse],
    value = "Parallelai-50: Change User Information")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "user data",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.UserProfile",
      paramType = "body",
      value = "information to change"),
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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

  @Path("/{account}")
  @ApiOperation(position=7,httpMethod = "DELETE", response = classOf[StatusResponse],
    value = "Parallelai-52: Delete User")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @Path("/{account}/points")
  @ApiOperation(position=8,httpMethod = "GET", response = classOf[UserPoints],
    value = "Parallelai-54: Get points gained by the user")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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

  @Path("/{account}/brand")
  @ApiOperation(position=9,httpMethod = "POST", response = classOf[StatusResponse],
    value = "Parallelai-90: Add Brand to User")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "brand data",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.BrandIDRequest",
      paramType = "body",
      value = "BrandId to add"),
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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

  @Path("/{account}/brand/{brandId}")
  @ApiOperation(position=10,httpMethod = "DELETE", response = classOf[StatusResponse],
    value = "Parallelai-90: Remove User Brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user"),
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to remove"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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

  @Path("/{account}/brand")
  @ApiOperation(position=11,httpMethod = "GET", response = classOf[List[BrandRecord]],
    value = "Parallelai-69: Get Brands for User")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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

  @Path("/application")
  @ApiOperation(position=12,httpMethod = "POST", response = classOf[AddApplicationResponse],
    value = "Parallelai-101: Add Application to existing user")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "application apply",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.AddApplicationRequest",
      paramType = "body",
      value = "application request")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
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

  @Path("/{account}/brand/{brand}/ads")
  @ApiOperation(position=13,httpMethod = "GET", response = classOf[List[AdvertDetailResponse]],
    value = "Parallelai-59: Suggested ads for User/Brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to query ads for"),
    new ApiImplicitParam(name = "max", required = true, dataType = "String", paramType = "query",
      value = "max number of suggested ads"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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

  @Path("/{account}/suggestedBrands")
  @ApiOperation(position=14,httpMethod = "POST", response = classOf[StatusResponse],
    value = "Parallelai-70: Add Suggested Brand to user")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(
      name = "application apply",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.BrandIDRequest",
      paramType = "body",
      value = "brand to add"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters"),
    new ApiResponse(code = 401, message = "Authentication Error")
  ))
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
  @Path("/{account}/suggestedBrands")
  @ApiOperation(position=15,httpMethod = "GET", response = classOf[List[BrandRecord]],
    value = "Parallelai-70: Get Suggested Brands for user")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
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

  @Path("/{account}/brand/{brand}")
  @ApiOperation(position=15,httpMethod = "GET", response = classOf[GetActiveAccountBrandOffersResponse],
    value = "Parallelai-123: Get Number of valid ads for user/brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
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

  @Path("/{account}/acceptedoffers")
  @ApiOperation(position=16,httpMethod = "GET", response = classOf[KaredoSalesApi],
    value = "Parallelai-124: Get offers for which user has requested the code")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user to query"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
      value = "SessionId for authentication/authorization")
  ))
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
