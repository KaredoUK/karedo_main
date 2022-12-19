package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.Kar141_SendCode_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar141_SendCode extends KaredoRoute
  with Kar141_SendCode_actor {

  def route = {
    Route {

      // POST /account
      path("account") {
              post {
                entity(as[Kar141_SendCode_Req]) {
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
