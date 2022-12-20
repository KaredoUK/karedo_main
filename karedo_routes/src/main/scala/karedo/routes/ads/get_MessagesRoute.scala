package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.get_MessagesActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object get_MessagesRoute extends KaredoRoute
  with get_MessagesActor {

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
