package karedo.actors.sale

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar186_postSale_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoUtils
{

  override val logger = LoggerFactory.getLogger(classOf[Kar186_postSale_actor])

  def exec(
            deviceId: Option[String],
            saleId: String,
           request: Kar186Req
          ): Result[Error, APIResponse] = {
    val accountId = request.account_id
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val sale = dbSale.find(saleId).get
          val app_karedos = karedos_to_appKaredos(sale.karedos)
          val sender_id = accountId
          val userProfile = dbUserProfile.find(sender_id).get
          val sender_name = s"${userProfile.last_name}, ${userProfile.first_name}"
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

            sendSMS(sender_msisdn, s"Transaction Complete. Sent ${app_karedos} Karedos to ${receiver_msisdn} (${receiver_name}).")
            sendSMS(receiver_msisdn, s"Transaction Complete. Received ${app_karedos} Karedos from ${sender_msisdn} (${sender_name}).")

            OK(APIResponse("", HTTP_OK_200))
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}