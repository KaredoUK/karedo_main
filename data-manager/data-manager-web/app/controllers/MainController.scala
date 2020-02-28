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
import api.{DataManagerMockClient, DataManagerRestClient, DataManagerApiClient}
import config.AppConfigInjection
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

object forms {

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

  def formToRegistrationValidation(appId: String, activationCode: String): RegistrationValidation = {
    RegistrationValidation(applicationIdFromString(appId), activationCode)
  }
}

import forms._
trait RegistrationController extends Controller {
  def dataManagerApiClient : DataManagerApiClient

  def index = Action {
    Ok(views.html.index.render("Hello from Data Manager Web UI"))
  }

  def register = Action {
    Ok(views.html.register.render(UUID.randomUUID().toString))
  }

  def submitRegistration = async { implicit request : Request[_] =>

    registrationForm.bindFromRequest.fold (

      hasErrors = {
        form => Future.successful( BadRequest("Invalid request") )
      },

      success = {
        registrationRequest => {
          dataManagerApiClient.register(registrationRequest) map {
            response =>
              Ok(views.html.confirmActivation.render(response.channel, response.address, response.applicationId.toString))

          } recoverWith {
            redirectToForFailedRequestAndFailForOtherCases(routes.MainController.register)
          }
        }
      }
    )

  }

  def confirmActivation = async { implicit request : Request[_] =>

    confirmActivationForm.bindFromRequest.fold (
      hasErrors = {
        form => Future.successful( BadRequest("Invalid request") )
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
             redirectToForFailedRequestAndFailForOtherCases(routes.MainController.confirmActivation)
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

  def editProfile = async {
    // Retrieve profile
    Future {
      Ok(views.html.editProfile.render(UserProfile(UserInfo("", None, None, None, None, None), UserSettings(100))))
    }
  }

  def updateProfile = async {
    Future {
      Ok(views.html.registrationCompleted.render())
    }
  }
}

import config.ConfigConversions._
object MainController extends RegistrationController with AppConfigInjection {
  val dataManagerApiClient : DataManagerApiClient = {
    implicit val _ = bindingModule

    bindingModule.injectPropertyOptional[Boolean]("useMockApi") match {
      case Some(true) => new DataManagerMockClient
      case _ => new DataManagerRestClient
    }
  }
}
