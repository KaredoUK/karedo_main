package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar188Actor
import karedo.entity.UserAd

/**
  * Created by pakkio on 10/3/16.
  */
object Kar188 extends KaredoRoute
  with Kar188Actor {

  def route = {
    Route {

      // GET /account/{{account_id}}/profile?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "profile") {
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
