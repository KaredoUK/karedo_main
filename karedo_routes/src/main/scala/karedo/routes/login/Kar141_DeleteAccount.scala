package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.Kar141_DeleteAccount_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar141_DeleteAccount extends KaredoRoute
  with Kar141_DeleteAccount_actor {

  def route = {
    Route {

      // DELETE /account
      path("account") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            delete {
              entity(as[Kar141_DeleteAccount_Req]) {
                request =>
                  doCall({
                    exec(deviceId, request)
                  }
                  )
              }
            }
        }
      }
    }
  }
}
