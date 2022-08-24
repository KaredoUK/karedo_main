package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{Kar135Actor, Kar136Actor}

/**
  * Created by pakkio on 10/3/16.
  */
object Kar136 extends KaredoRoute
  with Kar136Actor {

  def route = {
    Route {

      // GET /account/{{account_id}}/messages?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "messages") {
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
