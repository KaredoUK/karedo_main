package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.intent.get_IntentActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object get_IntentRoute extends KaredoRoute
  with get_IntentActor {

  def route = {
    Route {

      // GET /account/{{account_id}}/intent/{{intent_id}}?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
            optionalHeaderValueByName("X_Identification") {
              deviceId =>
                get {
                  parameters('p, 's ?) {
                    (applicationId, sessionId) =>
                      doCall({
                        exec(accountId, deviceId, applicationId, sessionId,
                          (if (intentId != null && ! intentId.isEmpty && ! intentId.equals("0")) Some(intentId) else None ) )
                      }
                    )
                }
              }
          }
      }
    }
  }
}
