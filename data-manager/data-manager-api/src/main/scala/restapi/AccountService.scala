package restapi

//import _root_.api.DefaultJsonFormats
import com.wordnik.swagger.annotations.{Api => ApiDoc, _}
import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data._

import core.EditAccountActor.EditAccountError
import core.security.UserAuthService
import spray.httpx.marshalling.{CollectingMarshallingContext, Marshaller}
import spray.json.RootJsonFormat
import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import akka.pattern.ask
import core.{EditAccountActor, ResponseWithFailure, User, RegistrationActor}
import akka.util.Timeout
import core.RegistrationActor._
import EditAccountActor._
import spray.http._
import spray.http.StatusCodes._
import parallelai.wallet.entity.UserAccount
import core.EditAccountActor._
import java.util.UUID

trait RegistrationServiceActorComponent
  extends Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with AuthorizationSupport {
  protected def registrationActor: ActorRef
  protected def editAccountActor: ActorRef
  protected implicit val executionContext: ExecutionContext

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)
}

// PARALLELAI-77API: Create Account
@ApiDoc(value = "/account", description = "User Registration Operations")
trait Create extends RegistrationServiceActorComponent {

  @ApiOperation(
    httpMethod = "POST",
    response = classOf[RegistrationResponse],
    value = "Returns a pet based on ID")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "request",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.RegistrationResponse",
      paramType = "body",
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
}

// PARALLELAI-53API: Validate/Activate Account Application
@ApiDoc(value = "/account/application/validation", description = "User Validation")
trait Validate extends RegistrationServiceActorComponent {

  @ApiOperation(
    httpMethod = "POST",
    response = classOf[RegistrationResponse],
    value = "Returns a pet based on ID")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "request",
      required = true,
      dataType = "com.parallelai.wallet.datamanager.data.RegistrationValidationResponse",
      paramType = "path",
      value = "Details of the request")
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

}
// PARALLELAI-49
trait Reset extends RegistrationServiceActorComponent {
  def reset =
    path( JavaUUID / "application" / JavaUUID / "reset") {
      (accountId: UserID, applicationId: ApplicationID) =>
      put {
        complete {
          (registrationActor ? AddApplicationToKnownUserRequest(applicationId, accountId)).
            mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
        }
      }
    }
}
// PARALLELAI-51 get user profile
trait GetUserProfile extends RegistrationServiceActorComponent {
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
}
// PARALLELAI-50 update userprofile
trait EditUserProfile extends RegistrationServiceActorComponent {
  def editUserProfile =
    path(JavaUUID) { accountId: UserID =>
      userAuthorizedFor(canAccessUser(accountId))(executionContext) {
        userAuthContext =>

        put {
          handleWith {
            userProfile: UserProfile =>
              editAccountActor ! UpdateAccount(userProfile)
              ""
          }
        }
      }
    }
}
// PARALLELAI-52 delete userprofile
trait DeleteUserProfile extends RegistrationServiceActorComponent{
  def deleteUserProfile =
    path(JavaUUID) { accountId: UserID =>
    userAuthorizedFor( canAccessUser(accountId) )(executionContext) {
      userAuthContext =>
        delete {
          complete {
            (editAccountActor ? DeleteAccount(accountId)).
              mapTo[ResponseWithFailure[EditAccountError, String]]
          }
        }
      }
    }
}

// PARALLELAI-54API: Get User Points
trait GetPoints extends RegistrationServiceActorComponent {
  def getPoints =
    path( JavaUUID / "points") { accountId: UserID =>
      userAuthorizedFor( canAccessUser(accountId) )(executionContext) {
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
}
// PARALLELAI-90API: Add Brand to User
trait AddBrandToUser extends RegistrationServiceActorComponent {
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
}

// PARALLELAI-69API: Show User Brands
trait ShowUserBrands extends  RegistrationServiceActorComponent {
  def showUserBrands =
    path( JavaUUID / "brand") { accountId: UserID =>
    userAuthorizedFor( canAccessUser(accountId) )(executionContext) {
      userAuthContext =>
        get {
          complete {
            (editAccountActor ? ListBrandsRequest(accountId)).
              mapTo[ResponseWithFailure[EditAccountError, List[BrandRecord]]]
          }
        }
      }

    }

}
// PARALLELAI-101: Add Application to Existing User
trait AddApplication extends RegistrationServiceActorComponent {
  def addApplication =
    path( "application" ) {
      post {
        handleWith {
          addApplicationToUserRequest: AddApplicationRequest =>
            (registrationActor ? addApplicationToUserRequest).
              mapTo[ResponseWithFailure[RegistrationError, AddApplicationResponse]]
        }
      }
    }

}
// PARALLELAI-102 API: User Login
trait Login extends RegistrationServiceActorComponent {
  def login =
  // POST /account/$UserID/application/$ApplicationId/login {
    path (JavaUUID / "application" / JavaUUID / "login"){
      (accountId: UserID, applicationId: ApplicationID) =>
        post {
          handleWith {
            loginRequest: APILoginRequest =>

              (registrationActor ? LoginRequest(accountId, applicationId, loginRequest.password)).
                mapTo[ResponseWithFailure[RegistrationError, APISessionResponse]]
          }
        }
    }
}

class AccountService(protected val registrationActor: ActorRef,
                     protected val editAccountActor: ActorRef,
                     override protected val userAuthService: UserAuthService)
                    (protected implicit val executionContext: ExecutionContext)
  extends Directives

  with Create
  with Validate
  with Reset
  with AddApplication
  with Login

  with GetUserProfile
  with EditUserProfile
  with DeleteUserProfile
  with GetPoints

  with AddBrandToUser
  with ShowUserBrands
{
  val route =
    pathPrefix("account") {
      create ~
      validate ~
      reset ~
      addApplication ~
      login ~
      getUserProfile ~
      editUserProfile ~
      deleteUserProfile ~
      getPoints ~
      addBrandToUser ~
      showUserBrands
    }
}
