package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.ads.Kar134_adsActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar134_ads extends KaredoRoute
  with Kar134_adsActor {

  def route = {
    Route {
      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}
      path("account" / Segment / "ads") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              get {
                parameters('p, 's ?, 'c ?) {
                  (applicationId, sessionId, adCount) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId, adCount)
                    }
                    )
                }
              }
          }
      }
    }
  }
}
