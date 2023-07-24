package karedo.route.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers, KaredoQRCode}
import karedo.route.routes.KaredoRoute
import org.slf4j.LoggerFactory
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections

/**
  * Created by pakkio on 10/3/16.
  */
object get_SaleQRRoute extends KaredoRoute
  with get_SaleQRActor {

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

trait get_SaleQRActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoQRCode {
  override val logger = LoggerFactory.getLogger(classOf[get_SaleQRActor])

  def exec(saleId: String): Result[Error, APIResponse] = {
    getQRCode(saleId) match {
      case OK(s) => OK(APIResponse(QRCodeResponse(s).toJson.toString, HTTP_OK_200))
      case KO(e) => KO(e)
    }
  }
}


