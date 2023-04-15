package karedo.route.routes.sale

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections

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

trait post_SaleActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{

  override val logger = LoggerFactory.getLogger(classOf[post_SaleActor])

  def exec(
            deviceId: Option[String],
            saleIdOrig: String,
            request: post_SaleRequest
          ): Result[Error, APIResponse] = {
    val accountId = request.account_id
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val saleId = saleIdOrig.trim.toUpperCase

          val sale = dbSale.find(saleId).get
          val app_karedos = karedos_to_appKaredos(sale.karedos)
          val sender_id = accountId
          val userProfile = dbUserProfile.find(sender_id).get
          val sender_name = s"${userProfile.last_name.get}, ${userProfile.first_name.get}"
          val sender_msisdn = dbUserAccount.find(accountId).get.findActiveMobile.get.msisdn

          if(sale.sale_type != TRANS_TYPE_SEND_RECEIVE || sale.status != TRANS_STATUS_OPEN )
            OK(APIResponse("Not Found", HTTP_NOTFOUND_404))
          else {

            moveKaredosBetweenAccounts(sender_id, sale.receiver_id, Some(sale.karedos))

            dbSale.update(sale.copy(
              sender_id = accountId, sender_name = sender_name, sender_msisdn = sender_msisdn,
              status = TRANS_STATUS_COMPLETE, ts_updated = now, ts_completed = Some(now)
            ))

            val receiver_msisdn = sale.receiver_msisdn
            val receiver_name = sale.receiver_name

            sendSMS(sender_msisdn, transfer.txt.sender_sale_success.render(app_karedos, receiver_msisdn, receiver_name).toString)
            sendSMS(receiver_msisdn, transfer.txt.receiver_sale_success.render(app_karedos, sender_msisdn, sender_name).toString)

            OK(APIResponse("", HTTP_OK_200))
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}