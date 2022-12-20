package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.intent.put_IntentActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object put_IntentRoute extends KaredoRoute
  with put_IntentActor {

  def route = {
    Route {

      // PUT /account/{{account_id}}/intent
      path("account" / Segment / "intent" ) {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              put {
                entity(as[IntentUpdateRequest]) {
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
