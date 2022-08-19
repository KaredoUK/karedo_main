package karedo.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar134Actor

/**
  * Created by pakkio on 10/3/16.
  */
trait Kar134 extends KaredoRoute
  with Kar134Actor {
  //def nl2br(s: String) = s.replace("\n", "<br>")


  def kar134 = {
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
