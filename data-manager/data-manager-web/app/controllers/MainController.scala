package controllers

import api.{DataManagerRestClient, DataManagerApiClient}
import parallelai.wallet.config.AppConfigInjection
import play.api.mvc.Action
import play.api.mvc.Results._
import play.mvc.Controller


object MainController extends Controller {
  def index = Action {
    Ok(views.html.index.render("Hello from Data Manager Web UI"))
  }
}

