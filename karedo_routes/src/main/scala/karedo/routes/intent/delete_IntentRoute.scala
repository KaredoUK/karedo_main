package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.intent.delete_IntentActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object delete_IntentRoute extends KaredoRoute
  with delete_IntentActor {

  def route = {
    Route {

      // DELETE /account/{{account_id}}/intent/{{intent_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              delete {
                entity(as[delete_IntentRequest]) {
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
