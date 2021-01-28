package restapi

import javax.ws.rs.Path

import com.wordnik.swagger.annotations.{Api => ApiDoc, _}
import core.objAPI.APIError
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data._

import core.EditAccountActor.EditAccountError
import core.security.UserAuthService
import spray.routing._
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern.ask
import core.{SuccessResponse, ResponseWithFailure}
import akka.util.Timeout
import core.RegistrationActor._
import core.EditAccountActor._
import java.util.UUID

// All APIs starting with /account go here
@ApiDoc(value = "/account", description = "Operations on the account.", position = 0)
abstract class AccountHttpService(
                                   protected val registrationActor: ActorRef,
                                   protected val editAccountActor: ActorRef,
                                   protected val brandActor: ActorRef,
                                   override protected val userAuthService: UserAuthService)
                                 (protected implicit val executionContext: ExecutionContext)

  extends HttpService
  with Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with AuthorizationSupport {

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  def route =
    pathPrefix("account") {
      create ~               // P77 POST /account
        validate ~           // P53 POST /account/validation/validate
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
        suggestedBrandsGet   // P70 GET AUTH /account/xxx/suggestedbrands
    } ~ pathPrefix("user") {
      userBrandInteraction // P55 POST AUTH /account/interaction/brand/xxx

    }

  // PARALLELAI-77API: Create Account

  @ApiOperation(httpMethod = "POST", response = classOf[RegistrationRequest], value = "Parallelai-77: Create a new Account")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "request", required = true,
      dataType = "com.parallelai.wallet.datamanager.data.RegistrationRequest", paramType = "body",
      value = "Details of the request")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Invalid Parameters")
  ))
  def create = pathEnd {
    post {
      handleWith {
        registrationRequest: RegistrationRequest =>
          (registrationActor ? registrationRequest).
            mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
      }
    }
  }

  // PARALLELAI-53API: Validate/Activate Account Application
  @Path("/application/validation")
  @ApiOperation(httpMethod = "POST", response = classOf[RegistrationValidationResponse],
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

  // PARALLELAI-49
  @Path("/{account}/application/{application}/reset")
  @ApiOperation(httpMethod = "PUT", response = classOf[RegistrationResponse],
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
      (accountId: UserID, applicationId: ApplicationID) =>
        put {
          complete {
            (registrationActor ? AddApplicationToKnownUserRequest(applicationId, accountId)).
              mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
          }
        }
    }

  @Path("/{account}/application/{application}/login")
  @ApiOperation(httpMethod = "POST", response = classOf[APISessionResponse],
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
      (accountId: UserID, applicationId: ApplicationID) =>
        post {
          handleWith {
            loginRequest: APILoginRequest =>

              (registrationActor ? LoginRequest(accountId, applicationId, loginRequest.password)).
                mapTo[ResponseWithFailure[RegistrationError, APISessionResponse]]
          }
        }
    }

  @Path("/{account}")
  @ApiOperation(httpMethod = "GET", response = classOf[UserProfile],
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
  @ApiOperation(httpMethod = "PUT", response = classOf[String],
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
                  mapTo[ResponseWithFailure[EditAccountError,String]]
            }
          }
      }
    }

  @Path("/{account}")
  @ApiOperation(httpMethod = "DELETE", response = classOf[String],
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
                mapTo[ResponseWithFailure[EditAccountError, String]]
            }
          }
      }
    }
  @Path("/{account}/points")
  @ApiOperation(httpMethod = "GET", response = classOf[UserPoints],
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
  @ApiOperation(httpMethod = "POST", response = classOf[String],
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
                  mapTo[ResponseWithFailure[EditAccountError, String]]
            }
          }
      }
    }

  @Path("/{account}/brand/{brandId}")
  @ApiOperation(httpMethod = "DELETE", response = classOf[String],
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
                mapTo[ResponseWithFailure[EditAccountError, String]]
            }
          }
        }
      }
    }

  @Path("/{account}/brand")
  @ApiOperation(httpMethod = "GET", response = classOf[List[BrandRecord]],
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
  @ApiOperation(httpMethod = "POST", response = classOf[AddApplicationResponse],
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
  @ApiOperation(httpMethod = "GET", response = classOf[List[AdvertDetailResponse]],
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
               // (brandActor ? RequestSuggestedAdForUsersAndBrand(accountId, brandId, max)).
               // mapTo[List[SuggestedAdForUsersAndBrand]]
                (brandActor ? ListBrandsAdverts(brandId,max)).
                  mapTo[ResponseWithFailure[APIError,List[AdvertDetailResponse]]]

              }
            }
          }
        }
      }
    }

  @Path("/{account}/suggestedBrands")
  @ApiOperation(httpMethod = "POST", response = classOf[String],
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
              (editAccountActor ? AddBrand(accountId, brandIdRequest.brandId)).mapTo[ResponseWithFailure[EditAccountError, String]]
          }
        }
      }
    }
  @Path("/{account}/suggestedBrands")
  @ApiOperation(httpMethod = "GET", response = classOf[List[BrandRecord]],
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
  @Path("/{account}/interaction/brand/{brand}")
  @ApiOperation(httpMethod = "GET", response = classOf[InteractionResponse],
    value = "Parallelai-55: User interacting with brand")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "account", required = true, dataType = "String", paramType = "path",
      value = "UUID of user interacting"),
    new ApiImplicitParam(name = "brand", required = true, dataType = "String", paramType = "path",
      value = "UUID of brand"),
    new ApiImplicitParam(name = "intType", required = true, dataType = "String", paramType = "body",
      value = "interactionType"),
    new ApiImplicitParam(name = "X-Session-Id", required = true, dataType = "String", paramType = "header",
          value = "SessionId for authentication/authorization")
  ))
  def userBrandInteraction: Route =

  // PARALLELAI-55API: User Brand Interaction
  // "user/"+userId+"/interaction/brand/"+brandId, { "interactionType":  "BUY"}
    path(JavaUUID / "interaction" / "brand") {
      (user) => {
        userAuthorizedFor(canAccessUser(user))(executionContext) { userAuthContext =>
          post {
            handleWith((interaction: UserBrandInteraction) =>

              (brandActor ? interaction)
                .mapTo[ResponseWithFailure[APIError, InteractionResponse]]
              // s"{${q}userId${q}: ${q}$user${q},${q}userTotalPoints${q}:${q}500${q}}")
            )
          }
        }
      }

    }

}








