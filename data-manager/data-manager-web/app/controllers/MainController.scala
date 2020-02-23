package controllers

import play.api.mvc.{Action, Result, Controller}

object MainController extends Controller {
  def index = Action { Ok(views.html.index.render("Hello from Data Manager Web UI")) }

  def register = Action { Ok(views.html.register.render) }
  def submitRegistration = Action {
    //generate activation code and send

    Ok(views.html.confirmActivation.render)
    //implicit request => Redirect(routes.MainController.confirmActivation.absoluteURL(false) )
  }
  def confirmActivation = Action { Ok(views.html.registrationCompleted.render) }
}
