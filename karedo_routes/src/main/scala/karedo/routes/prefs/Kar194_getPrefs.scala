package karedo.routes.prefs

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.prefs.Kar194_getPrefs_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar194_getPrefs extends KaredoRoute
  with Kar194_getPrefs_actor {

  def route = {
    Route {

      // GET /account/{{account_id}}/prefs?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "prefs") {
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
