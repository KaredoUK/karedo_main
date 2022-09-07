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


trait Kar197_putSale_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoUtils
{

  override val logger = LoggerFactory.getLogger(classOf[Kar197_putSale_actor])

  def exec(
            deviceId: Option[String],
           request: Kar197Req
          ): Result[Error, APIResponse] = {
    val accountId = request.account_id
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val receiverAccount = uAccount match {
            case OK(ua) => ua
            case KO(_) => throw Error("BAM! BAM!")
          }
          val receiverProfile = dbUserProfile.find(receiverAccount.id).get
          val receiver_id = receiverAccount.id
          val receiver_name = s"${receiverProfile.last_name}, ${receiverProfile.first_name}"
          val receiver_msisdn = receiverAccount.findActiveMobile.get.msisdn

          // IF we got till here. Sender account exists.

          val karedos:Long = request.app_karedos * APP_KAREDO_CONV
          val sale_type = TRANS_TYPE_SEND_RECEIVE
          val trans_status = TRANS_STATUS_OPEN

          val sale_id = getNewSaleCode

          val origSale = Sale(id = sale_id,
            // Merchant - ID, Name, MSISDN
            receiver_id = receiver_id, receiver_name = receiver_name, receiver_msisdn = receiver_msisdn,
            // Customer - ID, Name, MSISDN
            // No Sender.
            // Karedos, Sale Type, Status
            karedos = karedos, sale_type = TRANS_TYPE_TRANSFER, status = TRANS_STATUS_OPEN,
            // Timestamps
            ts_created = now, ts_updated = now
          )

          dbSale.insertNew(origSale)

          OK(APIResponse(Kar197Res(sale_id).toJson.toString, HTTP_OK_200))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}