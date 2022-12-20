package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.sale.post_SaleActor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object post_SaleRoute extends KaredoRoute
  with post_SaleActor {

  def route = {
    Route {

      path("sale" / Segment / "complete" ) {
        saleId =>
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            post {
              entity(as[post_SaleRequest]) {
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
