package karedo.route.routes.termsabout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections


object get_TermsRoute extends KaredoRoute
  with TermsAboutActor {

  def route = {
    Route {
      path("terms" ) {
        get {
              doCall({
                exec(GET_TERM)
              }
              )
        }
      }
    }
  }
}

object get_AboutRoute extends KaredoRoute
  with TermsAboutActor {

  def route = {
    Route {
      path("about" ) {
        get {
          doCall({
            exec(GET_ABOUT)
          }
          )
        }
      }
    }
  }
}

object get_PrivacyRoute extends KaredoRoute
  with TermsAboutActor {

  def route = {
    Route {
      path("privacy" ) {
        get {
          doCall({
            exec(GET_PRIVACY)
          }
          )
        }
      }
    }
  }
}

object get_BaseRoute extends KaredoRoute with KaredoConstants
{
  def route = {
    Route {
      path( "" ) {
        get {
          doCall({
            OK(APIResponse(VERSION))
          }
          )
        }
      }
    }
  }
}

trait TermsAboutActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[TermsAboutActor])

  def exec(termsAbout: String): Result[Error, APIResponse] = {
    val msg: String = termsAbout match {
      case GET_TERM => terms.html.terms.render().toString
      case GET_ABOUT  => terms.html.about.render().toString
      case GET_PRIVACY  => terms.html.privacy.render().toString
      case _ => "<html><body><h1>INFO</h1></body></html>"
    }

    OK(APIResponse(msg = msg, code = HTTP_OK_200, mime = MIME_HTML))
  }
}