package karedo.routes.transfer

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.transfer.put_TransferActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object put_TransferRoute extends KaredoRoute
  with put_TransferActor {

  def route = {
    Route {

      // PUT /account/{{account_id}}/intent
      path("transfer" ) {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            put {
              entity(as[put_TransferRequest]) {
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
