package controllers

import api.{DataManagerRestClient, DataManagerApiClient}
import parallelai.wallet.config.AppConfigInjection
import play.api.mvc.Action._
import play.api.mvc.Results._
import play.mvc.Controller
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global

import api.authorization._

object MainController extends Controller with AppConfigInjection {
  implicit val _ = bindingModule

  val dataManagerApiClient : DataManagerApiClient = new DataManagerRestClient

  def index = async { request =>
    implicit val _ = request

    isAuthorized(request, dataManagerApiClient) flatMap { authorized =>
      if(authorized) {
        dataManagerApiClient.getUserProfile(readUUIDCookie(COOKIE_UUID).get) map { _ match {
            case Some(userProfile) => Ok(views.html.registered_index.render ( s"Hello '${userProfile.info.fullName}' from Data Manager Web UI") )
            case None => InternalServerError
          }
        }
      } else {
        successful( Ok(views.html.index.render("Hello from Data Manager Web UI")) )
      }
    }
  }
}

