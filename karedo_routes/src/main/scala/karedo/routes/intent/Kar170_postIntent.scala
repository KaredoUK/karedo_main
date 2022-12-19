package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.intent.Kar170_postIntent_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar170_postIntent extends KaredoRoute
  with Kar170_postIntent_actor {

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
