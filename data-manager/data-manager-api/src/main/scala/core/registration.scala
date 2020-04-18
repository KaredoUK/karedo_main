package core

import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import com.parallelai.wallet.datamanager.data._
import scala.concurrent.Future
import scala.async.Async._
import java.util.UUID
import spray.json._
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import parallelai.wallet.entity.UserAccount
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang.RandomStringUtils
import javax.management.InvalidApplicationException
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import parallelai.wallet.entity.ClientApplication
import scala.Some
import parallelai.wallet.entity.UserAccount
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import parallelai.wallet.entity.ClientApplication
import scala.Some
import parallelai.wallet.entity.UserAccount
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest

/**
 * We use the companion object to hold all the messages that the ``RegistrationActor``
 * receives.
 */
object RegistrationActor {


  def props( userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO) : Props =
    Props( classOf[RegistrationActor], userAccountDAO, clientApplicationDAO)

  sealed trait RegistrationError
  case object ApplicationAlreadyRegistered extends RegistrationError
  case object UserAlreadyRegistered extends RegistrationError
  case class InvalidRequest(reason: String) extends RegistrationError
  case object InvalidValidationCode extends RegistrationError
  case class InternalError(throwable: Throwable) extends RegistrationError

  implicit object RegistrationErrorJsonFormat extends RootJsonFormat[RegistrationError] {
    def write(error: RegistrationError) =  error match {
      case ApplicationAlreadyRegistered => JsString("ApplicationAlreadyRegistered")
      case UserAlreadyRegistered => JsString("UserAlreadyRegistered")
      case InvalidValidationCode => JsString("InvalidValidationCode")
      case InvalidRequest(reason) => JsObject(
        "type" -> JsString("InvalidRequest"),
        "data" -> JsObject { "reason" -> JsString(reason)  }
      )
      case InternalError(throwable) => JsObject(
        "type" -> JsString("InternalError"),
        "data" -> JsObject (
          "errorClass" -> JsString(throwable.getClass.getName),
          "errorMessage" -> JsString(throwable.getMessage)
        )
      )
    }
    def read(value: JsValue) = {
      value match {
        case JsString("InvalidValidationCode") => InvalidValidationCode
        case JsObject(attributes) => {
          attributes.get("type") match {
            case Some(JsString("InvalidRequest")) =>
              val data = attributes.get("data") map { _.asInstanceOf[JsObject] }
              data match {
                case None => InvalidRequest("Unknown reason")
                case Some(errorData) =>
                  val reason = errorData.fields.get("reason") map { _.toString } getOrElse ("Unknown")
                  InvalidRequest(reason)
              }
            case Some(JsString("InternalError")) =>
              val data = attributes.get("data") map { _.asInstanceOf[JsObject] }
              data match {
                case None => InternalError (new Exception (s"Exception of class Unknown, message: Unknown") )
                case Some(errorData) =>
                  val errorClass = errorData.fields.get("errorClass") map { _.toString } getOrElse ("Unknown")
                  val errorMessage = errorData.fields.get("errorMessage") map { _.toString } getOrElse ("Unknown")
                  InternalError (new Exception (s"Exception of class $errorClass, message: '$errorMessage'") )
              }
            case _ => InternalError(new Exception(s"Unmapped error '$value'"))
          }
        }
        case JsString(errorName) => InternalError(new Exception(s"Unmapped error '$errorName'"))
        case _ => InternalError(new Exception(s"Unmapped error '$value'"))
      }

    }
  }

  case class AddApplication(applicationId: ApplicationID, accountId: UserID)
}

/**
 * Registers the users. Replies with
 */
class RegistrationActor(userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO) extends Actor with ActorLogging {
  import RegistrationActor._

  import context.dispatcher

  // notice that we don't actually perform any DB operations.
  // that's for another template
  def receive: Receive = {
    case request: RegistrationRequest =>  replyToSender { registerUser(request) }
    case request: AddApplication =>  replyToSender { registerApplication(request) }
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
  
  def applicationNotRegistered(applicationId: ApplicationID) : Future[Option[RegistrationError]] =
    clientApplicationDAO.getById(applicationId) map { _  map { _ => ApplicationAlreadyRegistered } }
  
  def validUserIdentification(userIdentification : WithUserContacts) : Future[Option[RegistrationError]] =
    Future.successful { if(userIdentification.isValid) None else Some(InvalidRequest("Invalid user identification")) }

  def registerUser(registrationRequest: RegistrationRequest): Future[Either[RegistrationError, RegistrationResponse]] =
    withValidations(registrationRequest) ( validUserIdentification, (registrationRequest) => applicationNotRegistered(registrationRequest.applicationId) ) { processValidRegistrationRequest }

  def registerApplication(registrationRequest: AddApplication): Future[Either[RegistrationError, RegistrationResponse]] =
    withValidations(registrationRequest) ( request => applicationNotRegistered(request.applicationId) ) {
      _ =>  userAccountDAO.getById(registrationRequest.accountId) map {
              _ match {
                case None => Left(InvalidRequest("User doesn't exist"))
                case Some(userAccount) =>
                  activateApplication(
                    registrationRequest.applicationId,
                    userAccount.id,
                    UserContacts(userAccount.email, userAccount.msisdn),
                    newActivationCode
                  )
              }
            }
    }


  def processValidRegistrationRequest(registrationRequest: RegistrationRequest): Future[Either[RegistrationError, RegistrationResponse]] = async {
    val existingAccountOp: Option[UserAccount] = await {
      userAccountDAO.findByAnyOf(None, registrationRequest.msisdn, registrationRequest.email)
    }

    if (existingAccountOp.filter( { _.active } ).isDefined) {
      Left(UserAlreadyRegistered)
    } else {
      if(existingAccountOp.isDefined) {
        log.debug("Removing account of already registered but not active user {}", existingAccountOp.get)
        userAccountDAO.delete(existingAccountOp.get.id)
      }

      log.debug("Creating new account for request {}", registrationRequest)
      val (accountId, validationCode) = createNewUserAccount(registrationRequest)

      activateApplication(registrationRequest.applicationId, accountId, registrationRequest, validationCode)
    }
  }

  private def activateApplication(applicationId: ApplicationID, accountId: UserID, userContacts: WithUserContacts, validationCode: String) : Either[RegistrationError, RegistrationResponse] = {
    //activationMessageActor ! (registrationRequest, validationCode)

    if(userContacts.msisdn.isDefined)
      Right(RegistrationResponse(applicationId, "msisdn", userContacts.msisdn.get))
    else
      Right(RegistrationResponse(applicationId, "email", userContacts.email.get))
  }
  
  private def createNewUserAccount(registrationRequest: RegistrationRequest) : (UUID, String) = {
    val account = UserAccount( id = UUID.randomUUID(), email = registrationRequest.email, msisdn = registrationRequest.msisdn )

    val activationCode = newActivationCode

    log.info("Activation code for registration request of application {} is '{}'", registrationRequest.applicationId, activationCode)

    val firstApplication = ClientApplication(registrationRequest.applicationId, account.id, activationCode)

    userAccountDAO.insertNew(account, firstApplication)

    (account.id, activationCode)
  }

  def newActivationCode: String = {
    RandomStringUtils.randomAlphanumeric(6)
  }

  def validateUser(validation: RegistrationValidation) : Future[Either[RegistrationError, RegistrationValidationResponse]] = async {
    val clientAppOption = await {
      clientApplicationDAO.getById(validation.applicationId)
    }

    clientAppOption match {
      case None => Left(InvalidRequest(s"Unable to find client application with ID '${validation.applicationId}'"))

      case Some(clientApplication) =>
        // I don't care if the user validates twice. I just check the validation code
        if(clientApplication.activationCode == validation.validationCode) {
          val userOpt = await { userAccountDAO.getByApplicationId(validation.applicationId) }

          userOpt map { user => 
            clientApplicationDAO.update(clientApplication copy (active = true))
            userAccountDAO.setActive(user.id)

            Right(RegistrationValidationResponse(validation.applicationId, user.id))
          } getOrElse {
            Left(InternalError(new IllegalStateException(s"Unable to find user for valid application ID '${validation.applicationId}'")))
          }
        } else {
          Left(InvalidValidationCode)
        }
    }

  }
}
