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

class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._

  import akka.pattern.ask
  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  implicit object EitherErrorSelector extends ErrorSelector[RegistrationError] {
    def apply(error: RegistrationError): StatusCode = error match {
      case InvalidRequest => BadRequest
      case InvalidValidationCode => Unauthorized
      case InternalError(_) => InternalServerError
    }
  }

  val route =
    path("account") {
      post {
        handleWith {
          registrationRequest: RegistrationRequest => (registration ? registrationRequest).mapTo[Either[RegistrationError, RegistrationResponse]]
        }
      }
    } ~
    path("account" / "application" / "validation") {
      post {
        handleWith {
          registrationValidation: RegistrationValidation =>  (registration ? registrationValidation).mapTo[Either[RegistrationError, RegistrationValidationResponse]]
        }
      }
    }
}
