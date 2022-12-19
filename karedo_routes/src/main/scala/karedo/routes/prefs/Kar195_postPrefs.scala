package karedo.routes.prefs

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.prefs.Kar195_postPrefs_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar195_postPrefs extends KaredoRoute
  with Kar195_postPrefs_actor {

  def route = {
    Route {

      // POST /account/{{account_id}}/prefs
      path("account" / Segment / "prefs") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[Kar195Req]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, request)
                    }
                    )
                }
              }
          }
      }
    }
  }
}
