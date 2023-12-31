package karedo.route.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.UserMobile
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}
import karedo.route.routes.prefs.get_PrefsRoute.AUTH_HEADER_NAME

/**
  * Created by pakkio on 10/3/16.
  */
object post_EnterCodeRoute extends KaredoRoute
  with post_EnterCodeActor {

  def route = {
    Route {
      // POST /verify?e={email}&c={email_code}&a={account_id}
      path("verify" ) {
        optionalHeaderValueByName(AUTH_HEADER_NAME) {
          deviceId =>
            post {
              entity(as[post_EnterCodeRequest]) {
                request =>
                  doCall(
                    {
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

trait post_EnterCodeActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[post_EnterCodeActor])

  def exec( deviceId: Option[String],
            request:post_EnterCodeRequest): Result[Error, APIResponse] = {

    Try [Result[Error, APIResponse]] {
      logger.debug(s"Mobile Verify\nmsisdn: ${request.msisdn}\nsms_code: ${request.sms_code}\nApplicationID: ${request.application_id}")

      val msisdn = msisdnFixer(request.msisdn)
      val sms_code = request.sms_code
      val password = request.password

      val userApp = dbUserApp.find(request.application_id).get
      val account_id = userApp.account_id
      val userAccount = dbUserAccount.find(account_id).get
      val mobile = userAccount.mobile.filter(e => e.msisdn == msisdn).head
      val acc_sms_code = mobile.sms_code.get.trim

      if( acc_sms_code == sms_code ) {
        val restMobiles = userAccount.mobile.filter(e => ! e.msisdn.equals(msisdn) )

        val newMobile = mobile.copy(valid = true, ts_validated = Some(now))
        val newUserAccount = userAccount.copy(
          mobile = List(newMobile) ++ restMobiles,
          password = Some(getPasswordHash(account_id, password)),
          temp = false )

        dbUserAccount.update(newUserAccount)

        // @TODO: WHat should be done here? How does the mobile get into UserMObile?
        dbUserMobile.find(msisdn) match {
          case OK(userMobile) => {
            if( userMobile.account_id == account_id) OK(APIResponse(AccountIdResponse(account_id).toJson.toString, HTTP_OK_200))
            else KO(Error(s"Verification failed. Mobile $msisdn already registered to ${userMobile.account_id} bu trying for $account_id"))
          }
          case KO(_) => {
            dbUserMobile.insertNew(UserMobile(
              id = msisdn, account_id = account_id, active = true, ts_created = now, ts_updated = now))

            dbUserApp.update(userApp.copy(mobile_linked = true, ts = now))

            val mobileSaleRes = dbMobileSale.find(msisdn)

            if( mobileSaleRes.isOK) {
              val mobileSale = mobileSaleRes.get
              mobileSale.sale_ids.foreach(sale_id => {
                val sale = dbSale.find(sale_id).get

                moveKaredosBetweenAccounts(sale.sender_id, account_id, Some(sale.karedos),
                  s"Completing Transfer from ${sale.sender_id} to ${account_id} for ${sale.karedos} Karedos")

                dbSale.update(sale.copy(receiver_id = account_id, status = TRANS_STATUS_COMPLETE,
                  ts_updated = now, ts_completed = Some(now)))

                val app_karedos = karedos_to_appKaredos(sale.karedos)

                sendSMS(sale.sender_msisdn, transfer.txt.sender_transfer_success.render(app_karedos, msisdn).toString)
                sendSMS(msisdn, transfer.txt.receiver_transfer_success.render(app_karedos, sale.sender_msisdn, sale.sender_name ).toString)
              })
            }

            OK(APIResponse(AccountIdResponse(account_id).toJson.toString, HTTP_OK_200))
          }
        }
      } else {
        KO(Error("Validationd Error. Code Doesn't match"))
      }
    } match {
      case Success(s) => s
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }
}