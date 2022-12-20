package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.sale.put_SaleActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object put_SaleRoute extends KaredoRoute
  with put_SaleActor {

  def route = {
    Route {

      path("sale" ) {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            put {
              entity(as[put_SaleRequest]) {
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
