package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.Kar138_login_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar138_Login extends KaredoRoute
  with Kar138_login_actor {

  def route = {
    Route {

      // POST /account/{{account_id}}/application/{{application_id}}/login
      path("login") {
          post {
            entity(as[Kar138Req]) {
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
