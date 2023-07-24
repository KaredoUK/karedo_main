package karedo.route.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.EmailVerify
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.routes.KaredoRoute
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}
import karedo.route.routes.prefs.get_PrefsRoute.AUTH_HEADER_NAME

/**
  * Created by pakkio on 10/3/16.
  */
object put_ResendRoute extends KaredoRoute
  with put_ResendActor {

  def route = {
    Route {
      path("resend") {
        optionalHeaderValueByName(AUTH_HEADER_NAME) {
          deviceId =>
          put {
            entity(as[put_ResendRequest]) {
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

trait put_ResendEmailActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[put_ResendEmailActor])

  def exec(deviceId:Option[String], request:put_ResendEmailRequest): Result[Error, APIResponse] = {
    Try[Result[Error, APIResponse]] {
      val application_id = request.application_id
      val address = request.email

      logger.debug(s"Resend\nAddress: ${address}\nApplicationID: ${application_id}")

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





