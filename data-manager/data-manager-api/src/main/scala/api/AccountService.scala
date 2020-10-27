package api

import com.mongodb.casbah.Imports._
import com.parallelai.wallet.datamanager.data._

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
import RegistrationActor.AddApplicationToKnownUserRequest
import parallelai.wallet.entity.UserAccount
import core.EditAccountActor._
import java.util.UUID


class AccountService(registrationActor: ActorRef, editAccountActor: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats with ApiErrorsJsonProtocol {

  import akka.pattern.ask
  import scala.concurrent.duration._

  implicit val timeout = Timeout(20.seconds)

  val route =
    pathPrefix("account") {
      create ~ find ~ reset ~ edit ~ validateApp ~ getPoints ~ getBrands ~ addApplication
    } 
      
  lazy val getBrands =
    path( JavaUUID / "brand") { accountId: UserID =>
      post {
        handleWith {
          brandIdRequest: BrandIDRequest =>
            (editAccountActor ? AddBrand(accountId, brandIdRequest.brandId)).mapTo[ResponseWithFailure[EditAccountError, String]]
        }
      } ~ get {
        complete {
          (editAccountActor ? ListBrandsRequest(accountId)).mapTo[ResponseWithFailure[EditAccountError,List[BrandRecord]]]
        }
      }

    }

  lazy val getPoints =
    path( JavaUUID / "points") { accountId: UserID =>
      rejectEmptyResponse {
        get {
          complete {
            (editAccountActor ? GetAccountPoints(accountId)).mapTo[ResponseWithFailure[RegistrationError,Option[UserPoints]]]
          }
        }
      }
    }

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
    }

  lazy val validateApp =
    path( "application" / "validation") {
      post {
        handleWith {
          registrationValidation: RegistrationValidation => (registrationActor ? registrationValidation).mapTo[ResponseWithFailure[RegistrationError, RegistrationValidationResponse]]
        }
      }
    }

  lazy val addApplication =
    path( "application" ) {
      post {
        handleWith {
          addApplicationToUserRequest: AddApplicationRequest =>
            (registrationActor ? addApplicationToUserRequest).mapTo[ResponseWithFailure[RegistrationError, AddApplicationResponse]]
        }
      }
    }
  
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
  
  lazy val create =
    pathEnd {
      post {
        handleWith {
          registrationRequest: RegistrationRequest =>
            (registrationActor ? registrationRequest).mapTo[ResponseWithFailure[RegistrationError, RegistrationResponse]]
        }
      }
    }
}
