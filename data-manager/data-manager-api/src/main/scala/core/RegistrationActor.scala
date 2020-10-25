package core

import java.net.URI

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import core.BrandActor.InternalBrandError
import core.MessengerActor.SendMessage
import core.common.RequestValidationChaining
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import akka.actor.{Props, ActorLogging, ActorRef, Actor}
import com.parallelai.wallet.datamanager.data._
import scala.concurrent.{ExecutionContext, Future}
import scala.async.Async._
import java.util.UUID
import spray.json._
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import com.parallelai.wallet.datamanager.data._
import scala.Some
import parallelai.wallet.entity.UserAccount
import org.apache.commons.lang.math.RandomUtils
import org.apache.commons.lang.RandomStringUtils
import javax.management.InvalidApplicationException
import parallelai.wallet.entity.ClientApplication
import scala.Some
import parallelai.wallet.entity.UserAccount
import parallelai.wallet.entity.ClientApplication
import scala.Some
import parallelai.wallet.entity.UserAccount
import scala.concurrent.Future.successful
import scala.util.Try


/**
 * We use the companion object to hold all the messages that the ``RegistrationActor``
 * receives.
 */
object RegistrationActor {

  def props(userAccountDAO: UserAccountDAO, clientApplicationDAO: ClientApplicationDAO, messengerActor: ActorRef)(implicit bindingModule: BindingModule): Props =
    Props(new RegistrationActor(userAccountDAO, clientApplicationDAO, messengerActor))

  sealed trait RegistrationError

  case object ApplicationAlreadyRegistered extends RegistrationError

  case object UserAlreadyRegistered extends RegistrationError

  case class InvalidRegistrationRequest(reason: String) extends RegistrationError

  case object InvalidValidationCode extends RegistrationError

  case class InternalRegistrationError(throwable: Throwable) extends RegistrationError

  implicit object registrationErrorJsonFormat extends RootJsonWriter[RegistrationError] {
    def write(error: RegistrationError) = error match {
      case ApplicationAlreadyRegistered => JsString("ApplicationAlreadyRegistered")
      case UserAlreadyRegistered => JsString("UserAlreadyRegistered")
      case InvalidValidationCode => JsString("InvalidValidationCode")
      case InvalidRegistrationRequest(reason) => JsObject(
        "type" -> JsString("InvalidRequest"),
        "data" -> JsObject {
          "reason" -> JsString(reason)
        }
      )
      case InternalRegistrationError(throwable) => JsObject(
        "type" -> JsString("InternalError"),
        "data" -> JsObject(
          "errorClass" -> JsString(throwable.getClass.getName),
          "errorMessage" -> JsString(throwable.getMessage)
        )
      )
    }

  }

  case class AddApplicationToKnownUserRequest(applicationId: ApplicationID, accountId: UserID)


  def newActivationCode: String = {
    RandomStringUtils.randomAlphanumeric(6)
  }
}

/**
 * Registers the users. Replies with
 */
class RegistrationActor(userAccountDAO: UserAccountDAO, clientApplicationDAO: ClientApplicationDAO,
                        messengerActor: ActorRef)(implicit val bindingModule: BindingModule)
  extends Actor with ActorLogging with Injectable with RequestValidationChaining {

  import RegistrationActor._

  import context.dispatcher

  val uiServerAddress = injectProperty[String]("ui.web.server.address")

  // notice that we don't actually perform any DB operations.
  // that's for another template
  def receive: Receive = {
    case request: RegistrationRequest => replyToSender {
      registerUser(request)
    }
    case request: AddApplicationToKnownUserRequest => replyToSender {
      registerApplication(request)
    }
    case validation: RegistrationValidation => replyToSender {
      validateUser(validation)
    }
    case addApplication: AddApplicationRequest => replyToSender {
      addApplicationToUser(addApplication)
    }
  }

  def registerUser(registrationRequest: RegistrationRequest): ResponseWithFailure[RegistrationError, RegistrationResponse] =
    withValidations(registrationRequest)(validUserIdentification, applicationNotRegistered, noActiveAccountForMsisdnOrEmail) { request =>

      log.debug("Creating new account for request {}", request)
      val account = UserAccount(id = UUID.randomUUID(), email = request.email, msisdn = request.msisdn)

      val activationCode = newActivationCode

      val firstApplication = ClientApplication(request.applicationId, account.id, activationCode)

      userAccountDAO.insertNew(account, firstApplication)

      activateApplication(request.applicationId, account.id, request, activationCode)
    }

  def addApplicationToUser(addApplicationRequest: AddApplicationRequest): ResponseWithFailure[RegistrationError, AddApplicationResponse] =
    withValidations(addApplicationRequest)(validUserIdentification, applicationNotRegistered) { request =>
      log.debug("Adding new application to user for request {}", request)

      userAccountDAO.findByAnyOf(None, request.msisdn, request.email) match {
        case None =>
          log.debug("Cannot find any user with specified params {}", request)
          FailureResponse(InvalidRegistrationRequest("Unable to find user with specified identification"))

        case Some(userAccount) =>
          log.debug("Adding new application {} to account {}", request.applicationId, userAccount.id)
          val registrationResponse = registerApplication(AddApplicationToKnownUserRequest(request.applicationId, userAccount.id))

          registrationResponse map { resp => AddApplicationResponse(resp.applicationId, resp.channel, resp.address)  }
      }
  }


  def registerApplication(registrationRequest: AddApplicationToKnownUserRequest): ResponseWithFailure[RegistrationError, RegistrationResponse] =
    withValidations(registrationRequest)(applicationNotRegistered) { request =>

      userAccountDAO.getById(request.accountId) match {
        case None => FailureResponse(InvalidRegistrationRequest(s"User with ID ${registrationRequest.accountId} doesn't exist"))

        case Some(userAccount) =>
          val activationCode = newActivationCode

          clientApplicationDAO.insertNew(ClientApplication(registrationRequest.applicationId, registrationRequest.accountId, activationCode))

          activateApplication(
            registrationRequest.applicationId,
            userAccount.id,
            UserContacts(userAccount.email, userAccount.msisdn),
            activationCode
          )
      }
    }

  def validateUser(validation: RegistrationValidation): ResponseWithFailure[RegistrationError, RegistrationValidationResponse] = {
    val clientAppOption = clientApplicationDAO.getById(validation.applicationId)

    clientAppOption match {
      case None => FailureResponse(InvalidRegistrationRequest(s"Unable to find client application with ID '${validation.applicationId}'"))

      case Some(clientApplication) =>
        // I don't care if the user validates twice. I just check the validation code
        if (clientApplication.activationCode == validation.validationCode) {
          val userOpt = userAccountDAO.getByApplicationId(validation.applicationId)

          userOpt map { user =>
            clientApplicationDAO.update(clientApplication copy (active = true))
            userAccountDAO.setActive(user.id)

            SuccessResponse(RegistrationValidationResponse(validation.applicationId, user.id))
          } getOrElse {
            FailureResponse(InternalRegistrationError(new IllegalStateException(s"Unable to find user for valid application ID '${validation.applicationId}'")))
          }
        } else {
          FailureResponse(InvalidValidationCode)
        }
    }

  }

  private def activateApplication(applicationId: ApplicationID, accountId: UserID, userContacts: WithUserContacts, validationCode: String): ResponseWithFailure[RegistrationError, RegistrationResponse] = {
    log.info("activateApplication: Validation code for registration request of application {} is '{}'", applicationId, validationCode)

    val activationMessage = s"Welcome to Karedo, your activation code is $validationCode. " +
      s"Please click on $uiServerAddress/confirmActivation?applicationId=$applicationId&activationCode=$validationCode"

    if (userContacts.msisdn.isDefined) {
      messengerActor ! SendMessage(URI.create(s"sms:${userContacts.msisdn.get}"), activationMessage)
      SuccessResponse(RegistrationResponse(applicationId, "msisdn", userContacts.msisdn.get))
    }
    else {
      messengerActor ! SendMessage(URI.create(s"mailto:${userContacts.email.get}"), activationMessage, "Welcome to Karedo")
      SuccessResponse(RegistrationResponse(applicationId, "email", userContacts.email.get))
    }

  }

  def replyToSender[T <: Any](response: => ResponseWithFailure[RegistrationError, T]): Unit = {
    Try {
      response
    } recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalRegistrationError(t))
    } foreach {
      responseContent: ResponseWithFailure[RegistrationError, T] =>
        sender ! responseContent
    }
  }

  def applicationNotRegistered(withApplicationId: {def applicationId: ApplicationID}): Option[RegistrationError] =
    clientApplicationDAO.getById(withApplicationId.applicationId) map { _ => ApplicationAlreadyRegistered}

  def validUserIdentification(userIdentification: WithUserContacts): Option[RegistrationError] =
    if (userIdentification.isValid) None else Some(InvalidRegistrationRequest("Invalid user identification"))

  // This validation has side effects!!!
  def noActiveAccountForMsisdnOrEmail(userIdentification: WithUserContacts): Option[RegistrationError] = {
    val existingAccountOp = userAccountDAO.findByAnyOf(None, userIdentification.msisdn, userIdentification.email)

    if (existingAccountOp.filter({
      _.active
    }).isDefined) {
      Some(UserAlreadyRegistered)
    } else {
      if (existingAccountOp.isDefined) {
        log.debug("Removing account of already registered but not active user {}", existingAccountOp.get)
        userAccountDAO.delete(existingAccountOp.get.id)
      }

      None
    }
  }

}
