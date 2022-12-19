package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.intent.Kar171_putIntent_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar171_putIntent extends KaredoRoute
  with Kar171_putIntent_actor {

  def route = {
    Route {

      // PUT /account/{{account_id}}/intent
      path("account" / Segment / "intent" ) {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              put {
                entity(as[Kar170Req]) {
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
