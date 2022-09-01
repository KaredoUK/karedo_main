package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.Kar166_interaction_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar166_interaction extends KaredoRoute
  with Kar166_interaction_actor {

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
