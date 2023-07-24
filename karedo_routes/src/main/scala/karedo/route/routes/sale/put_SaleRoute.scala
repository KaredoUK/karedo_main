package karedo.route.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{Sale, UserAccount, UserApp}
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections

/**
  * Created by pakkio on 10/3/16.
  */
object put_SaleRoute extends KaredoRoute
  with put_SaleActor {

  def route = {
    Route {

      path("sale" ) {
        optionalHeaderValueByName(AUTH_HEADER_NAME) {
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

trait put_SaleActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{

  override val logger = LoggerFactory.getLogger(classOf[put_SaleActor])

  def exec(
            deviceId: Option[String],
            request: put_SaleRequest
          ): Result[Error, APIResponse] = {
    val accountId = request.account_id
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val receiverAccount = uAccount.get

          val receiverProfile = dbUserProfile.find(receiverAccount.id).get
          val receiver_id = receiverAccount.id
          val receiver_name = s"${receiverProfile.last_name.get}, ${receiverProfile.first_name.get}"
          val receiver_msisdn = receiverAccount.findActiveMobile.get.msisdn

          // IF we got till here. Sender account exists.

          val karedos:Long = appKaredos_to_karedos(request.app_karedos)
          val sale_type = TRANS_TYPE_SEND_RECEIVE
          val trans_status = TRANS_STATUS_OPEN

          val sale_id = getNewSaleCode.toUpperCase

          val origSale = Sale(id = sale_id,
            // Merchant - ID, Name, MSISDN
            receiver_id = receiver_id, receiver_name = receiver_name, receiver_msisdn = receiver_msisdn,
            // Customer - ID, Name, MSISDN
            // No Sender.
            // Karedos, Sale Type, Status
            karedos = karedos, sale_type = sale_type, status = trans_status,
            // Timestamps
            ts_created = now, ts_updated = now
          )

          dbSale.insertNew(origSale)

          OK(APIResponse(SaleIdResponse(sale_id).toJson.toString, HTTP_OK_200))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}