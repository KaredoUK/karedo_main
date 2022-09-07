package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.sale.Kar199_getSaleQR_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar199_getSaleQR extends KaredoRoute
  with Kar199_getSaleQR_actor {

  def route = {
    Route {

      path("sale" / Segment / "qr" ) {
        saleId =>
              get {
                    doCall({
                      exec(saleId)
                    }
                    )
                }
              }
    }
  }
}
