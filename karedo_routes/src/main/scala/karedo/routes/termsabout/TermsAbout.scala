package karedo.routes.termsabout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.APIResponse
import karedo.actors.termsabout.TermsAboutActor
import karedo.routes.KaredoRoute
import karedo.util.KaredoConstants
import karedo.util.{KO, OK, Result}


object get_TermsRoute extends KaredoRoute
  with TermsAboutActor {

  def route = {
    Route {
      path("terms" ) {
        get {
              doCall({
                import karedo.util.KaredoConstants
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
            import karedo.util.KaredoConstants
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
            import karedo.util.KaredoConstants
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
