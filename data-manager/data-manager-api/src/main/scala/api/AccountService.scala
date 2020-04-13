package api

import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import core.{User, RegistrationActor}
import akka.util.Timeout
import RegistrationActor._
import spray.http._
import spray.http.StatusCodes._
import com.parallelai.wallet.datamanager.data._
import ApiDataJsonProtocol._
import RegistrationActor.AddApplication
import parallelai.wallet.entity.UserAccount
import core.EditAccountActor.{FindAccount, GetAccount}

class AccountService(registrationActor: ActorRef, editAccountActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  implicit object EitherErrorSelector extends ErrorSelector[RegistrationError] {
    def apply(error: RegistrationError): StatusCode = error match {
      case InvalidRequest(reason) => BadRequest
      case ApplicationAlreadyRegistered => BadRequest
      case UserAlreadyRegistered => BadRequest
      case InvalidValidationCode => Unauthorized
      case InternalError(_) => InternalServerError
    }
  }

  val route =
    path("account") {
      post {
        handleWith {
          registrationRequest: RegistrationRequest =>
            (registrationActor ? registrationRequest).mapTo[Either[RegistrationError, RegistrationResponse]]
        }
      } ~
      get {
        parameterMap { params =>
          rejectEmptyResponse {
            complete {
              (editAccountActor ? FindAccount(params.get("msisdn"), params.get("email"))).mapTo[Option[UserProfile]]
            }
          }
        }
      }
    }~
    path( "account" / JavaUUID ) { accountId: UserID =>
      rejectEmptyResponse {
        get {
          complete {
            (editAccountActor ? GetAccount(accountId)).mapTo[Option[UserProfile]]
          }
        }
      }
    } ~
    path("account" / JavaUUID / "application" / JavaUUID ) { (accountId: UserID, applicationId: ApplicationID) =>
      put {
        complete {
          (registrationActor ? AddApplication(accountId, applicationId)).mapTo[Either[RegistrationError, RegistrationResponse]]
        }
      }
    } ~
    path("account" / "application" / "validation") {
      post {
        handleWith {
          registrationValidation: RegistrationValidation =>  (registrationActor ? registrationValidation).mapTo[Either[RegistrationError, RegistrationValidationResponse]]
        }
      }
    }
}
