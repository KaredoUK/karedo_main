package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar194Actor

/**
  * Created by pakkio on 10/3/16.
  */
object Kar194 extends KaredoRoute
  with Kar194Actor {

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
