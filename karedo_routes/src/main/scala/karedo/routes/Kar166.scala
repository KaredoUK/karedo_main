package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{Kar136Actor, Kar166Actor}
import karedo.entity.UserAd

/**
  * Created by pakkio on 10/3/16.
  */
object Kar166 extends KaredoRoute
  with Kar166Actor {

  def route = {
    Route {

      // POST /account/{account_id}/ad/interaction
      path("account" / Segment / "ad" / "interaction") {
        accountId =>
          parameters('s ?) {
            sessionId => {
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[Kar166Request]) {
                      request =>
                        doCall({
                          exec(accountId, sessionId, deviceId, request)
                        }
                        )
                    }
                  }
              }
            }
          }

      }
    }
  }
}
