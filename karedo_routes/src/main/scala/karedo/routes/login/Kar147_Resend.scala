package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.Kar147_Resend_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar147_Resend extends KaredoRoute
  with Kar147_Resend_actor {

  def route = {
    Route {
      path("resend") {
          put {
            entity(as[Kar147_Resend]) {
              request =>
                doCall({
                  exec(request)
                }
                )
            }
          }
      }
    }
  }
}
