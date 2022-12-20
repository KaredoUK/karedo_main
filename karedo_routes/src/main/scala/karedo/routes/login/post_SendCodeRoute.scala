package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.post_SendCodeActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_SendCodeRoute extends KaredoRoute
  with post_SendCodeActor {

  def route = {
    Route {

      // POST /account
      path("account") {
              post {
                entity(as[post_SendCodeRequest]) {
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
