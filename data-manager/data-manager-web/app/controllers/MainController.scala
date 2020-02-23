package controllers

import play.api.mvc.{Action, Result, Controller}
import play.api.data._
import play.api.data.Forms._
import com.parallelai.wallet.datamanager.data._
import java.util.UUID

object forms {

  val registration = Form(
    mapping(
      "applicationId" -> text,
      "email" -> optional(email),
      "msisdn" -> optional(text)
    ) (formToRegistrationRequest)
      ( { registrationRequest : RegistrationRequest => Some(registrationRequest.applicationId.toString, registrationRequest.email, registrationRequest.msisdn) } )
  )

  def formToRegistrationRequest(appId: String, email: Option[String], msisdn: Option[String]) : RegistrationRequest =  {
    RegistrationRequest(applicationIdFromString(appId), email, msisdn)
  }
}


object MainController extends Controller {




  def index = Action { Ok(views.html.index.render("Hello from Data Manager Web UI")) }

  def register = Action { Ok(views.html.register.render(UUID.randomUUID().toString)) }
  def submitRegistration = Action {
    //generate activation code and send

    Ok(views.html.confirmActivation.render("msisdn", "00000001"))
    //implicit request => Redirect(routes.MainController.confirmActivation.absoluteURL(false) )
  }
  def confirmActivation = Action { Ok(views.html.registrationCompleted.render()) }
}
