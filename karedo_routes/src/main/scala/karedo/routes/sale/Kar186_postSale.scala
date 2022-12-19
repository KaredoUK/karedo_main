package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.sale.Kar186_postSale_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar186_postSale extends KaredoRoute
  with Kar186_postSale_actor {

  def route = {
    Route {

      path("sale" / Segment / "complete" ) {
        saleId =>
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            post {
              entity(as[Kar186Req]) {
                request =>
                  doCall({
                    exec(deviceId, saleId, request)
                  }
                  )
              }
            }
        }
      }
    }
  }
}
