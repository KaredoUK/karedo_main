package karedo.routes.termsabout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.APIResponse
import karedo.actors.termsabout.TermsAbout
import karedo.routes.KaredoRoute
import karedo.util.KaredoConstants
import karedo.util.{KO, OK, Result}


object Terms extends KaredoRoute
  with TermsAbout {

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

object About extends KaredoRoute
  with TermsAbout {

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

object Privacy extends KaredoRoute
  with TermsAbout {

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

object Base extends KaredoRoute with KaredoConstants
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
