package karedo.actors.login

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.UserSession
import karedo.util._
import org.slf4j.LoggerFactory
import scala.util.{Try, Success, Failure}
import karedo.util.Util.now

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar147_Resend_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[Kar147_Resend_actor])

  def exec(request:Kar147_Resend): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val application_id = request.application_id
      val msisdn = request.msisdn

      logger.info(s"Resend\nMobile: ${msisdn}\nApplicationID: ${application_id}")

      dbUserApp.find(application_id) match {
        case OK(userApp) => {
         dbUserMobile.find(msisdn) match {
           case OK(userMobile) => {
             if( userApp.account_id == userMobile.account_id) {
               val userAccount = dbUserAccount.find(userMobile.account_id).get

               userAccount.mobile.filter(x => x.msisdn == msisdn) match {
                 case mh::mt => {
                   val sms_code = getNewSMSCode
                   val mobile = mh.copy(valid = false, ts_validated = None, sms_code = Some(sms_code))

                   dbUserAccount.update(userAccount.copy(mobile = List(mobile)
                     ++ userAccount.mobile.filter(x => x.msisdn != msisdn)))

                   val sms_text = s"Welcome to Karedo. You're on your way to gaining from your attention. Code is [$sms_code]. Start the Karedo App to activate it"
                   sendSMS(msisdn, sms_text)

                   OK(APIResponse("", HTTP_OK_200))
                 }
                 case Nil => KO(Error("Mobile note registerd to account."))
               }
             } else {
               OK(APIResponse(ErrorRes(HTTP_CONFLICT_409, None, "Mobile doesn't match").toJson.toString, HTTP_CONFLICT_409))
             }
           }
           case KO(_) => {
             OK(APIResponse(ErrorRes(HTTP_GONE_410, None, "Something went terribly wrong").toJson.toString, HTTP_GONE_410))
           }
         }
        }
        case KO(_) => OK(APIResponse(ErrorRes(HTTP_NOTFOUND_404, None, "Mobile never used beforw").toJson.toString, HTTP_NOTFOUND_404))
      }

    } match {
      case Success(s) => s
      case Failure(f) => MAKE_ERROR(f)
    }
  }
}