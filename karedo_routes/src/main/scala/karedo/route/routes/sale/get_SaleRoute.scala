package karedo.route.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity._
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory
import spray.json._

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections


/**
  * Created by pakkio on 10/3/16.
  */
object get_SaleRoute extends KaredoRoute
  with get_SaleActor {

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

trait get_SaleActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants {

  override val logger = LoggerFactory.getLogger(classOf[get_SaleActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           saleIdOrig: String): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[Error, APIResponse]] {
          val saleId = saleIdOrig.trim.toUpperCase

          val sale = dbSale.find(saleId).get

          val newSale = sale.copy(karedos = karedos_to_appKaredos(sale.karedos).toLong)

          //          if( sale.receiver_id != accountId) OK(APIResponse("Conflict", HTTP_CONFLICT_409))
          //          else
          OK(APIResponse(newSale.toJson.toString, code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}