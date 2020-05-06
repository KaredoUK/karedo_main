package controllers

import java.util.UUID

import api.{DataManagerRestClient, DataManagerApiClient}
import controllers.RegistrationController._
import parallelai.wallet.config.AppConfigInjection
import play.api.mvc.{Action, Controller}

trait ProfileController extends Controller {
  def dataManagerApiClient : DataManagerApiClient

  def editProfile = Action {
    NoContent
  }

  def updateProfile = Action {
    NoContent
  }
}

object ProfileController extends ProfileController with AppConfigInjection {
  implicit val _ = bindingModule

  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient
}
