package karedo.routes.transfer

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.transfer.Kar183_putTransfer_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar183_putTransfer extends KaredoRoute
  with Kar183_putTransfer_actor {

  def route = {
    Route {

      // PUT /account/{{account_id}}/intent
      path("transfer" ) {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            put {
              entity(as[Kar183Req]) {
                request =>
                  doCall({
                    exec(deviceId, request)
                  }
                  )
              }
            }
        }
      }
    }
  }
}
