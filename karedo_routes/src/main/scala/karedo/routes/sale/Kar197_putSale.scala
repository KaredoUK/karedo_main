package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.sale.Kar197_putSale_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar197_putSale extends KaredoRoute
  with Kar197_putSale_actor {

  def route = {
    Route {

      path("sale" ) {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            put {
              entity(as[Kar197Req]) {
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
