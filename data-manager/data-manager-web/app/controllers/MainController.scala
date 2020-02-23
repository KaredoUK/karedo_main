package controllers

import play.api.mvc.{Action, Result, Controller}

object MainController extends Controller {
  def index = Action { Ok(views.html.index.render("Hello from UserProfileWeb")) }

  def register = Action { Ok(views.html.register.render) }
}
