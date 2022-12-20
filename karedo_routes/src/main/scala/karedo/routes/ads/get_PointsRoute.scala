package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.get_PointsActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object get_PointsRoute extends KaredoRoute
  with get_PointsActor {

  def route = {
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
