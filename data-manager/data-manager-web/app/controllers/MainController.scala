package controllers

import restapi.{DataManagerRestClient, DataManagerApiClient}
import parallelai.wallet.config.AppConfigInjection
import play.api.Logger
import play.api.mvc.Action._
import play.api.mvc.Results._
import play.mvc.Controller
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global

import restapi.authorization._

object MainController extends Controller with AppConfigInjection {
  implicit val _ = bindingModule

  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient

  def index = async { request =>
    implicit val _ = request

    isKnownUser(request, dataManagerApiClient) flatMap { knownUser =>
      if(knownUser) {
        Logger.debug( "MainController.index: Known user found")
        dataManagerApiClient.getUserProfile(readUUIDCookie(COOKIE_UUID).get) map { _ match {
            case Some(userProfile) => Ok(views.html.registered_index.render ( s"${userProfile.info.fullName}", userProfile.totalPoints) )
            case None => InternalServerError
          }
        }
      } else {
        Logger.debug( "MainController.index: Unknown user found")
        successful( Ok(views.html.index.render()) )
      }
    }
  }
}

