package controllers

import java.util.UUID

import api.{DataManagerRestClient, DataManagerApiClient}
import com.parallelai.wallet.datamanager.data.{UserSettings, UserInfo, UserProfile, RegistrationRequest}
import controllers.RegistrationController._
import org.joda.time.DateTime
import parallelai.wallet.config.AppConfigInjection
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}
import play.api.mvc.{Action, Controller}
import Action._
import scala.concurrent.Future._

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global


object profileForms {
  val VALID_GENDERS = Set("M", "F")
  def validGender: Constraint[String] = Constraint[String]("Invalid gender") { value: String =>
    if(VALID_GENDERS.contains(value)) Valid else Invalid(ValidationError("Invalid gender"))
  }

  val updateProfileForm = Form(
    mapping(
      "info" -> mapping (
        "userId" -> nonEmptyText,
        "fullName" -> optional(text),
        "email" -> optional(email),
        "msisdn" -> optional(text),
        "birthDate" -> optional(jodaDate("dd-MM-yyyy")),
        "postCode" -> optional(text),
        "country" -> optional(text),
        "gender" -> optional(text verifying validGender)
      )(formToUserInfo)(
          (userInfo: UserInfo) => Some(
            (userInfo.userId.toString, Some(userInfo.fullName), userInfo.email, userInfo.msisdn, userInfo.birthDate, userInfo.postCode, userInfo.country, userInfo.gender)
          )
      ),

      "settings" -> mapping(
        "maxAdsPerWeek" -> number
      )(UserSettings.apply)(UserSettings.unapply),

      "totalPoints" -> longNumber

    )
    (UserProfile.apply) (UserProfile.unapply )
  )


  def formToUserInfo(id: String, name: Option[String], email: Option[String], msisdn: Option[String],
                     birthDate: Option[DateTime], postCode: Option[String], country: Option[String], gender: Option[String] ) =
    UserInfo(UUID.fromString(id), name getOrElse "", email, msisdn, postCode, country, birthDate, gender)
}

import api.authorization._
import profileForms._
trait ProfileController extends Controller {
  def dataManagerApiClient : DataManagerApiClient

  def editProfile = async { implicit request =>

    val userId = readUUIDCookie(COOKIE_UUID).get

    dataManagerApiClient.getUserProfile(userId) map {
      _ match {
        case Some(userProfile) => Ok(views.html.editProfile.render(userProfile))
        case None => Forbidden
      }
    }
  }

  def updateProfile = async { implicit request =>
    val userId = readUUIDCookie(COOKIE_UUID).get

    updateProfileForm.bindFromRequest.fold(
      hasErrors = {
        form => successful {
          BadRequest(form.errorsAsJson.toString)
        }
      },

      success = {
        userProfile =>
          dataManagerApiClient.updateUserProfile(
            userProfile copy( info = userProfile.info.copy(userId = userId))
          ) map { _ => Redirect(routes.MainController.index.absoluteURL(false)) } recover {
            case throwable: Throwable => InternalServerError(throwable.toString)
          }
      }
    )
  }
}

object ProfileController extends ProfileController with AppConfigInjection {
  implicit val _ = bindingModule

  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient
}
