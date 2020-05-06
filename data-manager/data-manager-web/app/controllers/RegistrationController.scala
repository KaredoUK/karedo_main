package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import com.parallelai.wallet.datamanager.data._
import java.util.UUID
import play.api.mvc.Action._
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import com.parallelai.wallet.datamanager.data.UserProfile
import scala.Some
import com.parallelai.wallet.datamanager.data.UserSettings
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import com.parallelai.wallet.datamanager.data.UserInfo
import api.{DataManagerRestClient, DataManagerApiClient}
import com.parallelai.wallet.datamanager.data.UserProfile
import scala.Some
import com.parallelai.wallet.datamanager.data.UserSettings
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import com.parallelai.wallet.datamanager.data.UserInfo
import scala.Some
import com.parallelai.wallet.datamanager.data.UserSettings
import com.parallelai.wallet.datamanager.data.UserProfile
import play.api.mvc.Cookie
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import com.parallelai.wallet.datamanager.data.UserInfo
import spray.client.UnsuccessfulResponseException
import play.api.mvc.Results._
import com.parallelai.wallet.datamanager.data.AddApplicationRequest
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import scala.Some
import play.api.mvc.SimpleResult
import play.api.mvc.Call
import com.parallelai.wallet.datamanager.data.UserProfile
import play.api.mvc.Cookie
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import parallelai.wallet.config.AppConfigInjection

object registrationForms {

  val registrationForm = Form(
    mapping(
      "applicationId" -> nonEmptyText,
      "email" -> optional(email),
      "msisdn" -> optional(text)
    )
    (formToRegistrationRequest)
    ( { registrationRequest : RegistrationRequest => Some(registrationRequest.applicationId.toString, registrationRequest.email, registrationRequest.msisdn) } )
    verifying (
      "You must provide your email or phone number.", {
        _ match {
          case RegistrationRequest(_, None, None) => false
          case _ => true
        }
      }
    )
  )

  val addApplicationForm = Form(
    mapping(
      "applicationId" -> nonEmptyText,
      "email" -> optional(email),
      "msisdn" -> optional(text)
    )
      (formToAddApplicationRequest)
    ( { registrationRequest : AddApplicationRequest => Some(registrationRequest.applicationId.toString, registrationRequest.email, registrationRequest.msisdn) } )
      verifying (
      "You must provide your email or phone number.", {
      _ match {
        case AddApplicationRequest(_, None, None) => false
        case _ => true
      }
    }
      )
  )

  val confirmActivationForm = Form(
    mapping(
      "applicationId" -> nonEmptyText,
      "activationCode" -> nonEmptyText
    )
    (formToRegistrationValidation)
    ( { registrationValidation: RegistrationValidation => Some(registrationValidation.applicationId.toString, registrationValidation.validationCode) })
  )

  def formToRegistrationRequest(appId: String, email: Option[String], msisdn: Option[String]) : RegistrationRequest =  {
    RegistrationRequest(applicationIdFromString(appId), msisdn, email)
  }

  def formToAddApplicationRequest(appId: String, email: Option[String], msisdn: Option[String]) : AddApplicationRequest =  {
    AddApplicationRequest(applicationIdFromString(appId), msisdn, email)
  }

  def formToRegistrationValidation(appId: String, activationCode: String): RegistrationValidation = {
    RegistrationValidation(applicationIdFromString(appId), activationCode)
  }
}

import registrationForms._
trait RegistrationController extends Controller {
  def dataManagerApiClient : DataManagerApiClient

  def badRequest(error: String)(implicit request: Request[_]) = BadRequest(views.html.errors.onBadRequest(request, error))

  def register = Action {
    Ok(views.html.register.render(UUID.randomUUID().toString, routes.RegistrationController.submitRegistration.url, "Register new account"))
  }

  def registerApplication = Action {
    Ok(views.html.register.render(UUID.randomUUID().toString, routes.RegistrationController.submitRegisterApplication.url, "Register new application"))
  }

  def submitRegistration = async { implicit request : Request[_] =>

    registrationForm.bindFromRequest.fold (

      hasErrors = {
        form => Future.successful( badRequest("Invalid request") )
      },

      success = {
        registrationRequest => {
          dataManagerApiClient.register(registrationRequest) map {
            response =>
              Ok(views.html.confirmActivation.render(response.channel, response.address, response.applicationId.toString))

          } recoverWith {
            redirectToForFailedRequestAndFailForOtherCases(routes.RegistrationController.register)
          }
        }
      }
    )

  }

  def getConfirmActivation = Action { implicit request: Request[_] =>
    confirmActivationForm.bindFromRequest.fold (
      hasErrors = {
        form => badRequest("Invalid request")
      },

      success = {
        confirmActivation => {
          Ok(views.html.confirmActivationDirect.render(confirmActivation.applicationId.toString, confirmActivation.validationCode))
        }
      }
    )
  }

  def submitRegisterApplication = async { implicit request : Request[_] =>
    addApplicationForm.bindFromRequest.fold (
      hasErrors = {
        form => Future.successful( badRequest("Invalid request") )
      },

      success = {
        addApplicationRequest => {
          val userProfileFutureOp : Future[Option[UserProfile]] = dataManagerApiClient.findUserByMsisdnOrEmail(addApplicationRequest.msisdn, addApplicationRequest.email)

          userProfileFutureOp flatMap {
            _ match {
              case Some(userProfile) =>
                val addApplicationResponseFuture = dataManagerApiClient.addApplication(userProfile.info.userId, addApplicationRequest.applicationId)
                addApplicationResponseFuture map { response =>
                  Ok(views.html.confirmActivation.render(response.channel, response.address, response.applicationId.toString))
                } recoverWith {
                  redirectToForFailedRequestAndFailForOtherCases(routes.RegistrationController.registerApplication)
                }
              case None =>
                Future.successful(badRequest("Cannot find User to add the application to"))
            }
          } recoverWith {
            redirectToForFailedRequestAndFailForOtherCases(routes.RegistrationController.registerApplication)
          }
        }
      }
    )
  }

  def confirmActivation = async { implicit request : Request[_] =>

    confirmActivationForm.bindFromRequest.fold (
      hasErrors = {
        form => Future.successful( badRequest("Invalid request") )
      },

      success = {
        validation => {

          dataManagerApiClient.validateRegistration(validation) map {
            validationResponse =>
              Ok(views.html.registrationCompleted.render())
                .withCookies(
                  Cookie("applicationId", validationResponse.applicationId.toString),
                  Cookie("uuid", validationResponse.userID.toString)
                )
          } recoverWith {
             redirectToForFailedRequestAndFailForOtherCases(routes.RegistrationController.confirmActivation)
          }
        }
      }
    )
  }

  def redirectToForFailedRequestAndFailForOtherCases(targetEndPoint: Call)(implicit request : Request[_]) : PartialFunction[Throwable, Future[SimpleResult]] = {
    case unsuccessfulResponse : UnsuccessfulResponseException =>
      Future.successful {
        unsuccessfulResponse.responseStatus match {
          case BadRequest | Unauthorized => Redirect(targetEndPoint.absoluteURL(false))
          case _ => InternalServerError(unsuccessfulResponse.responseStatus.toString)
        }
      }
    case exception => Future.successful(InternalServerError(exception.toString))
  }

  def editProfile = Action { NoContent }

  def updateProfile = Action {
    Ok(views.html.registrationCompleted.render())
  }
}

object RegistrationController extends RegistrationController with AppConfigInjection {
  implicit val _ = bindingModule

  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient
}
