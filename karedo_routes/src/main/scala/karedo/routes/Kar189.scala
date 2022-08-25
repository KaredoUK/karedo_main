package karedo.routes

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.Kar189Actor

/**
  * Created by pakkio on 10/3/16.
  */
object Kar189 extends KaredoRoute
  with Kar189Actor {

  def route = {
    Route {

      // POST /account/{{account_id}}/profile
      path("account" / Segment / "profile") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[Kar189Req]) {
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
