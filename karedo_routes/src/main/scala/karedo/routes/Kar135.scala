package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{Kar134Actor, Kar135Actor}

/**
  * Created by pakkio on 10/3/16.
  */
trait Kar135 extends KaredoRoute
  with Kar135Actor {

  def kar135 = {
    Route {

      // GET /account/{{account_id}}/points?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "points") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              get {
                parameters('p, 's ?) {
                  (applicationId, sessionId) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId)
                    }
                    )
                }
              }
          }
      }

    }

  }
}
