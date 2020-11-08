package restapi

import restapi.security.AuthorizationSupport
import com.parallelai.wallet.datamanager.data._

import core.EditAccountActor.EditAccountError
import core.security.UserAuthService
import spray.httpx.marshalling.{CollectingMarshallingContext, Marshaller}
import spray.json.RootJsonFormat
import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import core.{EditAccountActor, ResponseWithFailure, User, RegistrationActor}
import akka.util.Timeout
import RegistrationActor._
import EditAccountActor._
import spray.http._
import spray.http.StatusCodes._
import RegistrationActor.AddApplicationToKnownUserRequest
import parallelai.wallet.entity.UserAccount
import core.EditAccountActor._
import java.util.UUID


class AccountService(registrationActor: ActorRef,
                     editAccountActor: ActorRef,
                     override protected val userAuthService: UserAuthService)
                    (implicit executionContext: ExecutionContext)
  extends Directives
  with DefaultJsonFormats
  with ApiErrorsJsonProtocol
  with ApiDataJsonProtocol
  with AuthorizationSupport
{

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route =
    pathPrefix("account") {
      create ~ find ~ reset ~ edit ~
        validateApp ~ getPoints ~ getBrands ~
        addApplication ~ login
    }

  // PARALLELAI-77API: Create Account
  lazy val create =
    pathEnd {
      post {
        handleWith {
          registrationRequest: RegistrationRequest =>
            (registrationActor ? registrationRequest).mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
        }
      }
    }

  // seems orphan no PARALLELAI referring to this (utility function?)
  lazy val find =
    pathEnd {
      get {
        parameters('email.?, 'msisdn.?, 'applicationId.?) { (email, msisdn, applicationId) =>
          rejectEmptyResponse {
            complete {
              (editAccountActor ? FindAccount(applicationId map {
                UUID.fromString(_)
              }, msisdn, email)).mapTo[ResponseWithFailure[RegistrationError, Option[UserProfile]]]
            }
          }
        }
      }
    }

  // PARALLELAI-49
  lazy val reset =
    path( JavaUUID / "application" / JavaUUID / "reset") { (accountId: UserID, applicationId: ApplicationID) =>
      put {
        complete {
          (registrationActor ? AddApplicationToKnownUserRequest(applicationId, accountId)).mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
        }
      }
    }


  lazy val edit =
      path(JavaUUID) { accountId: UserID =>
        userAuthorizedFor( canAccessUser(accountId) )(executionContext) { userAuthContext =>
          rejectEmptyResponse {
            // PARALLELAI-51 get user profile
            get {
              complete {
                (editAccountActor ? GetAccount(accountId)).mapTo[ResponseWithFailure[EditAccountError, Option[UserProfile]]]
              }
            }
          } ~
            // PARALLELAI-50 update userprofile
            put {
              handleWith {
                userProfile: UserProfile =>
                  editAccountActor ! UpdateAccount(userProfile)
                  ""
              }
            } ~
            // PARALLELAI-52 delete userprofile
            delete {
              complete {
                (editAccountActor ? DeleteAccount(accountId)).mapTo[ResponseWithFailure[EditAccountError, String]]
              }
            }
        }
      }

  lazy val validateApp =
    path( "application" / "validation") {
      // review this because of new modifications
      // PARALLELAI-53API: Validate/Activate Account Application
      post {
        handleWith {
          registrationValidation: RegistrationValidation => (registrationActor ? registrationValidation).mapTo[ResponseWithFailure[RegistrationError, RegistrationValidationResponse]]
        }
      }
    }


  // PARALLELAI-54API: Get User Points
  lazy val getPoints =
    path( JavaUUID / "points") { accountId: UserID =>
      userAuthorizedFor( canAccessUser(accountId) )(executionContext) { userAuthContext =>
        rejectEmptyResponse {
          get {
            complete {
              (editAccountActor ? GetAccountPoints(accountId)).mapTo[ResponseWithFailure[RegistrationError, Option[UserPoints]]]
            }
          }
        }
      }
    }

  lazy val getBrands =
    path( JavaUUID / "brand") { accountId: UserID =>
      // PARALLELAI-90API: Add Brand to User
      post {
        handleWith {
          brandIdRequest: BrandIDRequest =>
            (editAccountActor ? AddBrand(accountId, brandIdRequest.brandId)).mapTo[ResponseWithFailure[EditAccountError, String]]
        }
      } ~
      // PARALLELAI-69API: Show User Brands
      get {
        complete {
          (editAccountActor ? ListBrandsRequest(accountId)).mapTo[ResponseWithFailure[EditAccountError,List[BrandRecord]]]
        }
      }

    }

  // PARALLELAI-101: Add Application to Existing User
  lazy val addApplication =
    path( "application" ) {
      post {
        handleWith {
          addApplicationToUserRequest: AddApplicationRequest =>
            (registrationActor ? addApplicationToUserRequest).mapTo[ResponseWithFailure[RegistrationError, AddApplicationResponse]]
        }
      }
    }


  lazy val login =
    // PARALLELAI-102 API: User Login
    // POST /account/$UserID/application/$ApplicationId/login {
    path (JavaUUID / "application" / JavaUUID / "login"){
      (accountId: UserID, applicationId: ApplicationID) =>
      post {
        handleWith {
          loginRequest: APILoginRequest =>

          (registrationActor ? LoginRequest(accountId, applicationId, loginRequest.password)).mapTo[ResponseWithFailure[RegistrationError, APISessionResponse]]
        }
      }
    }

}
