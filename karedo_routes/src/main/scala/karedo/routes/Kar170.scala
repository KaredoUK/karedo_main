package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar170Actor

/**
  * Created by pakkio on 10/3/16.
  */
object Kar170 extends KaredoRoute
  with Kar170Actor {

  def route = {
    Route {

      // POST /account/{{account_id}}/intent/{{intent_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[Kar170Req]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, intentId, request)
                    }
                    )
                }
              }
          }
      }
    }
  }
}
