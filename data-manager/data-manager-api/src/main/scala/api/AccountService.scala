package api

import com.mongodb.casbah.Imports._
import com.parallelai.wallet.datamanager.data._
import core.BrandActor.{InternalBrandError, InvalidBrandRequest, BrandError}
import core.EditAccountActor.EditAccountError
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
import com.parallelai.wallet.datamanager.data._
import ApiDataJsonProtocol._
import RegistrationActor.AddApplication
import parallelai.wallet.entity.UserAccount
import core.EditAccountActor._
import java.util.UUID


class AccountService(registrationActor: ActorRef, editAccountActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route =
    path("account") {
      post {
        handleWith {
          registrationRequest: RegistrationRequest =>
            (registrationActor ? registrationRequest).mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
        }
      } ~
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
    } ~
      path("account" / JavaUUID / "application" / JavaUUID / "reset") { (accountId: UserID, applicationId: ApplicationID) =>
        put {
          complete {
            (registrationActor ? AddApplication(applicationId, accountId)).mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
          }
        }
      } ~
      path("account" / JavaUUID) { accountId: UserID =>
        rejectEmptyResponse {
          get {
            complete {
              (editAccountActor ? GetAccount(accountId)).mapTo[ResponseWithFailure[EditAccountError, Option[UserProfile]]]
            }
          }
        } ~
          put {
            handleWith {
              userProfile: UserProfile =>
                editAccountActor ! UpdateAccount(userProfile)
                ""
            }
          } ~
          delete {
            complete {
              (editAccountActor ? DeleteAccount(accountId)).mapTo[ResponseWithFailure[EditAccountError, String]]
            }
          }
      } ~
      path("account" / "application" / "validation") {
        post {
          handleWith {
            registrationValidation: RegistrationValidation => (registrationActor ? registrationValidation).mapTo[ResponseWithFailure[RegistrationError, RegistrationValidationResponse]]
          }
        }
      } ~
      path("account" / JavaUUID / "points") { accountId: UserID =>
        rejectEmptyResponse {
          get {
            complete {
              (editAccountActor ? GetAccountPoints(accountId)).mapTo[ResponseWithFailure[RegistrationError,Option[UserPoints]]]
            }
          }
        }
      } ~
      path("account" / JavaUUID / "brand") { accountId: UserID =>
        rejectEmptyResponse {
          post {
            handleWith {
              brandIdRequest: BrandIDRequest =>
                (editAccountActor ? AddBrand(accountId, brandIdRequest.brandId)).mapTo[ResponseWithFailure[EditAccountError, String]]
            }
          }
        }
      }

}
