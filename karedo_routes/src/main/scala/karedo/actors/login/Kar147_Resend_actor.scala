package karedo.actors.login

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{EmailVerify, UserAccount, UserApp, UserSession}
import karedo.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.util.Util.now


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

                   val sms_text = welcome.txt.sms_verify.render(sms_code).toString
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
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }
}


trait Kar147_ResendEmail_actor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[Kar147_ResendEmail_actor])

  def exec(request:Kar147_ResetEmail): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val application_id = request.application_id
      val address = request.email

      logger.info(s"Resend\nAddress: ${address}\nApplicationID: ${application_id}")

      dbUserApp.find(application_id) match {
        case OK(userApp) => {
          dbUserEmail.find(address) match {
            case OK(userEmail) => {
              if( userApp.account_id == userEmail.account_id) {
                val userAccount = dbUserAccount.find(userEmail.account_id).get

                userAccount.email.filter(x => x.address == address) match {
                  case mh::mt => {
                    val email_code = getNewRandomID
                    val email = mh.copy(valid = false, ts_validated = None, email_code = Some(email_code))

                    dbUserAccount.update(userAccount.copy(email = List(email)
                      ++ userAccount.email.filter(x => x.address != address)))

                    val verify_id = getNewRandomID
                    dbEmailVerify.insertNew(EmailVerify(id = verify_id, account_id = userAccount.id, application_id = application_id))

                    val email_verify_url = s"${notification_base_url}/verify?e=${email.address}&c=${email_code}&v=${verify_id}"
                    val email_subject = "Welcome to Karedo"
                    val email_body = welcome.html.email_verify.render(email_verify_url).toString
                    sendEmail(address, email_subject, email_body)

                    OK(APIResponse("", HTTP_OK_200))
                  }
                  case Nil => KO(Error("Email note registerd to account."))
                }
              } else {
                OK(APIResponse(ErrorRes(HTTP_CONFLICT_409, None, "Email doesn't match").toJson.toString, HTTP_CONFLICT_409))
              }
            }
            case KO(_) => {
              OK(APIResponse(ErrorRes(HTTP_GONE_410, None, "Something went terribly wrong").toJson.toString, HTTP_GONE_410))
            }
          }
        }
        case KO(_) => OK(APIResponse(ErrorRes(HTTP_NOTFOUND_404, None, "Email never used beforw").toJson.toString, HTTP_NOTFOUND_404))
      }

    } match {
      case Success(s) => s
      case Failure(f) => MAKE_THROWN_ERROR(f)
    }
  }
}

trait Kar147_ValidateEmail_actor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[Kar147_ValidateEmail_actor])

  def exec(deviceId:Option[String], request:Kar147_ValidateEmail_Request): Result[Error, APIResponse] = {

    val application_id = request.application_id
    val account_id = request.account_id
    val session_id = request.session_id

    authenticate(account_id, deviceId, application_id, Some(session_id), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          uAccount.get.email.filter(x => x.address == request.email) match {
            case mh :: mt => {
              if(mh.valid) OK(APIResponse(Kar147_ValidateEmail_Res(true).toString.toString, code))
              else OK(APIResponse(Kar147_ValidateEmail_Res(false).toString.toString, code))
            }
            case Nil => OK(APIResponse(Kar147_ValidateEmail_Res(false).toString.toString, code))
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}

