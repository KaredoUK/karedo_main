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

import forms._
trait RegistrationController extends Controller {
  def dataManagerApiClient : DataManagerApiClient

  def index = Action {
    Ok(views.html.index.render("Hello from Data Manager Web UI"))
  }

  def register = Action {
    Ok(views.html.register.render(UUID.randomUUID().toString, routes.MainController.submitRegistration.url, "Register new account"))
  }

  def registerApplication = Action {
    Ok(views.html.register.render(UUID.randomUUID().toString, routes.MainController.submitRegisterApplication.url, "Register new application"))
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

  def submitRegisterApplication = async { implicit request : Request[_] =>
    addApplicationForm.bindFromRequest.fold (
      hasErrors = {
        form => Future.successful( BadRequest("Invalid request") )
      },

      success = {
        registrationRequest => {
          val userProfileFutureOp : Future[Option[UserProfile]] = dataManagerApiClient.findUser(registrationRequest.msisdn, registrationRequest.email)

          userProfileFutureOp flatMap {
            _ match {
              case Some(userProfile) =>
                println(s"Adding application ${userProfile.info.userId}  to user ${registrationRequest.applicationId}")
                val addApplicationResponseFuture = dataManagerApiClient.addApplication(userProfile.info.userId, registrationRequest.applicationId)
                addApplicationResponseFuture map { response =>
                  Ok(views.html.confirmActivation.render(response.channel, response.address, response.applicationId.toString))
                } recoverWith {
                  redirectToForFailedRequestAndFailForOtherCases(routes.MainController.registerApplication)
                }
              case None =>
                println("Cannot find user")
                Future.successful(BadRequest("Cannot find User to add the application to"))
            }
          } recoverWith {
            redirectToForFailedRequestAndFailForOtherCases(routes.MainController.registerApplication)
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

  def editProfile = Action { NoContent }

  def updateProfile = Action {
    Ok(views.html.registrationCompleted.render())
  }
}

import config.ConfigConversions._
object MainController extends RegistrationController with AppConfigInjection {
  implicit val _ = bindingModule

  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient
}
