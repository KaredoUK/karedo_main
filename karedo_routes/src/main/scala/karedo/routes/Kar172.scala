package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar172Actor

/**
  * Created by pakkio on 10/3/16.
  */
object Kar172 extends KaredoRoute
  with Kar172Actor {

  def route = {
    Route {

      // DELETE /account/{{account_id}}/intent/{{intent_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              delete {
                entity(as[Kar172Req]) {
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
