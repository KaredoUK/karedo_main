package karedo.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.login.delete_AccountActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object delete_AccountRoute extends KaredoRoute
  with delete_AccountActor {

  def route = {
    Route {

      // DELETE /account
      path("account") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            delete {
              entity(as[delete_AccountRequest]) {
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
