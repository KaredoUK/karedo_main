package karedo.actors.login

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{Mobile, UserMobile}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar145_EnterCode_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[Kar145_EnterCode_actor])

  def exec(request:Kar145Req): Result[Error, APIResponse] = {

    Try [Result[Error, APIResponse]] {
      logger.info(s"Mobile Verify\nmsisdn: ${request.msisdn}\nsms_code: ${request.sms_code}\nApplicationID: ${request.application_id}")

      val msisdn = request.msisdn
      val sms_code = request.sms_code
      val password = request.password

      val userApp = dbUserApp.find(request.application_id).get
      val account_id = userApp.account_id
      val userAccount = dbUserAccount.find(account_id).get
      val mobile = userAccount.mobile.filter(e => e.msisdn == msisdn).head
      val acc_sms_code = mobile.sms_code

      if( acc_sms_code == sms_code ) {
        val restMobiles = userAccount.mobile.filter(e => ! e.msisdn.equals(msisdn) )

        val newMobile = mobile.copy(valid = true, ts_validated = Some(now))
        val newUserAccount = userAccount.copy(
          mobile = List(newMobile) ++ restMobiles,
          password = Some(password),
          temp = false )

        dbUserAccount.update(newUserAccount)

        val userMobile = dbUserMobile.find(msisdn).get

        if( userMobile.account_id == account_id) KO(Error(s"Verification failed. Mobile already verified for $msisdn in $account_id"))
        else KO(Error(s"Verification failed. Mobile $msisdn already registered to ${userMobile.account_id} bu trying for $account_id"))

        dbUserMobile.insertNew(UserMobile(
          id = msisdn, account_id = account_id, active = true, ts_created = now, ts_updated = now))

        dbUserApp.update(userApp.copy(mobile_linked = true, ts = now))

        val mobileSale = dbMobileSale.find(msisdn).get
        mobileSale.sale_ids.foreach(sale_id => {
          val sale = dbSale.find(sale_id).get

          moveKaredosBetweenAccounts(sale.sender_id, account_id, Some(sale.karedos),
            s"Completing Transfer from ${sale.sender_id} to ${account_id} for ${sale.karedos} Karedos")

          dbSale.update(sale.copy(receiver_id = account_id, status = TRANS_STATUS_COMPLETE,
            ts_updated = now, ts_completed = Some(now)))

          sendSMS(sale.sender_id, s"Success. Transfer of ${sale.karedos} Karedos to ${account_id} Completed.")
          sendSMS(account_id, s"Success. Transfer of ${sale.karedos} Karedos from ${sale.sender_id} (${sale.sender_name}) Completed.")
        })

        OK(APIResponse(Kar145Res(account_id).toJson.toString, HTTP_OK_200))

      } else {
        KO(Error("Validationd Error. Code Doesn't match"))
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_ERROR(f)
    }
  }
}