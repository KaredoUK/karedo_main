package karedo.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.sale.Kar198_getSale_actor
import karedo.routes.KaredoRoute

/**
  * Created by pakkio on 10/3/16.
  */
object Kar198_getSale extends KaredoRoute
  with Kar198_getSale_actor {

  def route = {
    Route {

      path("sale" / Segment ) {
        saleId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              get {
                parameters('a, 'p, 's ?) {
                  (accountId, applicationId, sessionId) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId, saleId)
                    }
                    )
                }
              }
          }
      }
    }
  }
}
