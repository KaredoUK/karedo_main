package core

import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import akka.actor.{ActorLogging, ActorRef, Actor}
import com.parallelai.wallet.datamanager.data._
import scala.concurrent.Future
import scala.async.Async._
import java.util.UUID
import spray.json.{JsObject, JsString, JsValue, RootJsonFormat}
import parallelai.wallet.persistence.cassandra.{ClientApplicationCassandraDAO, UserAccountCassandraDAO}
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import parallelai.wallet.entity.UserAccount
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang.RandomStringUtils

/**
 * We use the companion object to hold all the messages that the ``RegistrationActor``
 * receives.
 */
object RegistrationActor {

  sealed trait RegistrationError
  case object ApplicationAlreadyRegistered extends RegistrationError
  case object UserAlreadyRegistered extends RegistrationError
  case object InvalidRequest extends RegistrationError
  case object InvalidValidationCode extends RegistrationError
  case class InternalError(throwable: Throwable) extends RegistrationError

  implicit object RegistrationErrorJsonFormat extends RootJsonFormat[RegistrationError] {
    def write(error: RegistrationError) =  error match {
      case ApplicationAlreadyRegistered => JsString("ApplicationAlreadyRegistered")
      case UserAlreadyRegistered => JsString("UserAlreadyRegistered")
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
class RegistrationActor extends Actor with ActorLogging {
  import RegistrationActor._

  import context.dispatcher

  val userAccountDAO : UserAccountDAO = new UserAccountCassandraDAO()
  val clientApplicationDAO : ClientApplicationDAO = new ClientApplicationCassandraDAO()

  // notice that we don't actually perform any DB operations.
  // that's for another template
  def receive: Receive = {
    case request: RegistrationRequest =>  replyToSender { registerUser(request) }
    case validation: RegistrationValidation => replyToSender { validateUser(validation) }
  }

  def replyToSender[T <: Any](response: Future[Either[RegistrationError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        Left(InternalError(t))
    } foreach {
      response : Either[RegistrationError, T] =>
        replyTo ! response
    }
  }

  def withValidations[Request, Error, Response](request: Request)(validations: (Request => Future[Option[Error]])*) (successFlow: Request => Future[Either[Error, Response]]) : Future[Either[Error, Response]] = {
    val validationResult = validations.foldLeft[Future[Option[Error]]](Future.successful(None)) { 
      case (currStatus, currFunction) =>  
        currStatus flatMap { _ match {
          case Some(_) => currStatus
          case None => currFunction(request)
          }
        }
    }
    validationResult flatMap {
      _ match {
        case Some(validationError) => Future.successful(Left(validationError))
        case None => successFlow(request)
      } 
    }
  } 
  
  def applicationNotRegistered(registrationRequest : RegistrationRequest) : Future[Option[RegistrationError]] =
    clientApplicationDAO.getById(registrationRequest.applicationId) map { _  map { _ => ApplicationAlreadyRegistered } }
  
  def validRegistrationRequest(registrationRequest : RegistrationRequest) : Future[Option[RegistrationError]] = 
    Future.successful { if(registrationRequest.isValid) None else Some(InvalidRequest) }
  
  def registerUser(registrationRequest: RegistrationRequest): Future[Either[RegistrationError, RegistrationResponse]] = 
    withValidations(registrationRequest)  ( validRegistrationRequest, applicationNotRegistered(_) ) { processValidRegistrationRequest }

  def processValidRegistrationRequest(registrationRequest: RegistrationRequest): Future[Either[RegistrationError, RegistrationResponse]] = async {
    val existingAccountOp: Option[UserAccount] = await {
      userAccountDAO.findByAnyOf(None, registrationRequest.msisdn, registrationRequest.email)
    }

    if (existingAccountOp.filter( { _.active } ).isDefined) {
      Left(UserAlreadyRegistered)
    } else {
      val accountId = existingAccountOp match {
        case None =>
          log.debug("Creating new account for request {}", registrationRequest)
          createNewUserAccount(registrationRequest)

        case Some(account) =>
          log.debug("Updating account of already registered user {}", account)
          userAccountDAO.update(account.copy(msisdn = registrationRequest.msisdn, email = registrationRequest.email))
          account.id
      }

      val activationCode = RandomStringUtils.random(6)

      clientApplicationDAO.insertNew(ClientApplication(registrationRequest.applicationId, accountId, activationCode))

      //activationMessageActor ! registrationRequest

      if(registrationRequest.msisdn.isDefined)
        Right(RegistrationResponse(registrationRequest.applicationId, "msisdn", registrationRequest.msisdn.get))
      else
        Right(RegistrationResponse(registrationRequest.applicationId, "email", registrationRequest.email.get))
    }
  }

  private def createNewUserAccount(registrationRequest: RegistrationRequest) : UUID = {
    val account = UserAccount( id = UUID.randomUUID(), email = registrationRequest.email, msisdn = registrationRequest.msisdn )
    userAccountDAO.insertNew(account)

    account.id
  }

  def validateUser(validation: RegistrationValidation) : Future[Either[RegistrationError, RegistrationValidationResponse]] = async {
    val clientAppOption = await { clientApplicationDAO.getById(validation.applicationId) }

    clientAppOption match {
      case None => Left(InvalidRequest)

      case Some(clientApplication) =>
        // I don't care if the user validate twice. I just check the validation code
        if(clientApplication.activationCode == validation.validationCode) {
          val userOpt = await { userAccountDAO.getByApplicationId(validation.applicationId) }

          userOpt map { user => 
            Right(RegistrationValidationResponse(validation.applicationId, user.id)) 
          } getOrElse {
            Left(InternalError(new IllegalStateException(s"Unable to find user for valid application ID ${validation.applicationId}")))
          }
        }
        else {
          Left(InvalidRequest)
        }
    }

  }


}
