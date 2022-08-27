package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar171Actor

/**
  * Created by pakkio on 10/3/16.
  */
object Kar171 extends KaredoRoute
  with Kar171Actor {

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
