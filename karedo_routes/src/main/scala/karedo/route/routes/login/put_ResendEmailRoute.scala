package karedo.route.routes.login

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}

/**
  * Created by charaj on 16/04/2017.
  */
object put_ResendEmailRoute extends KaredoRoute
  with put_ResendEmailActor {

  def route = {
    Route {
      path("resend" / "email") {
        put {
          entity(as[put_ResendEmailRequest]) {
            request =>
              doCall({
                exec(request)
              }
              )
          }
        }
      }
    }
  }
}

trait put_ResendActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[put_ResendActor])

  def exec(request:put_ResendRequest): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val application_id = request.application_id
      val msisdn = msisdnFixer(request.msisdn)

      logger.debug(s"Resend\nMobile: ${msisdn}\nApplicationID: ${application_id}")

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
