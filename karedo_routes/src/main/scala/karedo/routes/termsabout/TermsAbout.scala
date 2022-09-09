package karedo.routes.termsabout

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.termsabout.TermsAbout
import karedo.routes.KaredoRoute

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

