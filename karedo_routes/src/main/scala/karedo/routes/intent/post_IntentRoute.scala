package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.intent.post_IntentActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_IntentRoute extends KaredoRoute
  with post_IntentActor {

  def route = {
    Route {

      // POST /account/{{account_id}}/intent/{{intent_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[IntentUpdateRequest]) {
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
