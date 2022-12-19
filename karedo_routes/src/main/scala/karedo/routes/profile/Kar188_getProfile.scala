package karedo.routes.profile

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.profile.Kar188_getProfile_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar188_getProfile extends KaredoRoute
  with Kar188_getProfile_actor {

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
