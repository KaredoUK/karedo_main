package karedo.route.routes.login

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}

/**
  * Created by charaj on 16/04/2017.
  */
object get_ValidateSessionRoute extends KaredoRoute
  with get_ValidateSessionActor {

  def route = {
    Route {
      path("validate" / "session") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            get {
              parameters( 'a, 'p, 's ? ) {
                (account_id, application_id, session_id) =>
                  doCall({
                    exec(deviceId, account_id, application_id, session_id)
                  })
              }
            }
        }
      }
    }
  }
}

trait get_ValidateSessionActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[post_ValidateEmailActor])

  def exec(deviceId:Option[String], account_id:String, application_id:String, session_id:Option[String]): Result[Error, APIResponse] = {

    authenticate(account_id, deviceId, application_id, session_id, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val uApp_AccountId = uapp.get.account_id
          val uAccId = uAccount.get.id

          dbUserSession.find(session_id.get) match {
            case OK(uSession) => {
              val uSess_AccountId = uSession.account_id

              if( uApp_AccountId != uAccId || uAccId != uSess_AccountId ) OK(APIResponse(ValidateBooleanResponse(false).toJson.toString.toString, code))
              else OK(APIResponse(ValidateBooleanResponse(true).toJson.toString.toString, code))
            }
            case KO(_) => OK(APIResponse(ValidateBooleanResponse(false).toJson.toString.toString, code))
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}
