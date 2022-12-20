package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.post_LoginActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_LoginRoute extends KaredoRoute
  with post_LoginActor {

  def route = {
    Route {

      // POST /account/{{account_id}}/application/{{application_id}}/login
      path("login") {
          post {
            entity(as[post_LoginRequest]) {
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
