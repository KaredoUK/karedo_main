package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar141Actor_SendCode

/**
  * Created by pakkio on 10/3/16.
  */
object Kar141_SendCode extends KaredoRoute
  with Kar141Actor_SendCode {

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
