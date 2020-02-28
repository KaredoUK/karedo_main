package core

import akka.actor.{ActorRef, Actor}
import com.parallelai.wallet.datamanager.data._
import scala.concurrent.Future
import scala.async.Async.{async, await}
import java.util.UUID
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}

/**
 * We use the companion object to hold all the messages that the ``RegistrationActor``
 * receives.
 */
object RegistrationActor {

  sealed trait RegistrationError
  case object InvalidRequest extends RegistrationError
  case object InvalidValidationCode extends RegistrationError
  case class InternalError(throwable: Throwable) extends RegistrationError

  implicit object RegistrationErrorJsonFormat extends RootJsonFormat[RegistrationError] {
    def write(error: RegistrationError) =  error match {
      case InvalidRequest => JsString("InvalidRequest")
      case InvalidValidationCode => JsString("InvalidValidationCode")
      case InternalError(throwable) => JsObject(
        "errorClass" -> JsString(throwable.getClass.getName),
        "errorMessage" -> JsString(throwable.getMessage)
      )
    }
    def read(value: JsValue) = {
      value match {
        case JsString("InvalidRequest") => InvalidRequest
        case JsString("InvalidValidationCode") => InvalidValidationCode
        case JsObject(attributes) => {
          val errorClass = attributes.get("errorClass").getOrElse("Unknown")
          val errorMessage = attributes.get("errorMessage").getOrElse("Unknown")
          InternalError(new Exception(s"Exception of class $errorClass, message: '$errorMessage'"))
        }
        case JsString(errorName) => InternalError(new Exception(s"Unmapped error '$errorName'"))
        case _ => InternalError(new Exception(s"Unmapped error '$value'"))
      }

    }
  }
}

/**
 * Registers the users. Replies with
 */
class RegistrationActor extends Actor{
  import RegistrationActor._

  import context.dispatcher

  // notice that we don't actually perform any DB operations.
  // that's for another template
  def receive: Receive = {
    case request: RegistrationRequest =>  replyToSender { registerUser(request) }
    case validation: RegistrationValidation => replyToSender { validateUser(validation) }
  }

  def replyToSender[T <: Any](response: Future[Either[RegistrationError, T]]): Unit = {
    val replyTo = sender

    response recover { case t => Left(InternalError(t)) } foreach { response : Either[RegistrationError, T] => replyTo ! response }
  }

  def registerUser(registrationRequest: RegistrationRequest): Future[Either[RegistrationError, RegistrationResponse]] = async {
    registrationRequest match {
      case RegistrationRequest(appId, Some(msisdn), emailOption) => {
        Right(RegistrationResponse(appId, "msisdn", msisdn))
      }
      case RegistrationRequest(appId, None, Some(email)) => {
        Right(RegistrationResponse(appId, "email", email))
      }
      case _ => Left(InvalidRequest)
    }
  }

  def validateUser(validation: RegistrationValidation) : Future[Either[RegistrationError, RegistrationValidationResponse]] = async {
    Right(RegistrationValidationResponse(validation.applicationId, UUID.randomUUID()))
  }

}
