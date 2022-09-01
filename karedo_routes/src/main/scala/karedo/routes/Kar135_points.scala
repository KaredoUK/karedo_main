package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{Kar134_adsActor, Kar135_pointsActor}

/**
  * Created by pakkio on 10/3/16.
  */
object Kar135_points extends KaredoRoute
  with Kar135_pointsActor {

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
