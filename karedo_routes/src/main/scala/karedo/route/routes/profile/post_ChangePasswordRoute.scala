package karedo.route.routes.profile

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}
import karedo.route.routes.profile.post_ProfileRoute.AUTH_HEADER_NAME

/**
  * Created by charaj on 17/04/2017.
  */
object post_ChangePasswordRoute extends KaredoRoute
  with post_ChangePasswordActor {

  def route = {
    Route {
      path("password" ) {
        optionalHeaderValueByName(AUTH_HEADER_NAME) {
          deviceId =>
          post {
            entity(as[post_ChangePasswordRequest]) {
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

trait post_ChangePasswordActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
{
  override val logger = LoggerFactory.getLogger(this.getClass.toString)

  def exec(deviceId: Option[String], request: post_ChangePasswordRequest): Result[Error, APIResponse] = {
    val account_id = request.account_id
    val application_id = request.application_id
    val session_id = Some(request.session_id)

    authenticate(account_id, None, application_id, session_id, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[Error, UserAccount]] {
          uAccount match {
            case OK(userAccount) => {
              userAccount.password match {
                case Some(storedPassword) => {
                  if( doesPasswordMatch(account_id, request.old_password, storedPassword)) {
                    val new_password = getPasswordHash(account_id, request.new_password)

                    val newAccount = userAccount.copy(password = Some(new_password), ts_updated = now)

                    dbUserAccount.update(newAccount) match {
                      case KO(k) => KO(Error(k, HTTP_SERVER_ERROR_500))
                      case ok @ OK(_) => ok
                    }
                  } else {
                    KO(Error("Password doesn't match", HTTP_CONFLICT_409))
                  }
                }
                case None => KO(Error("Password not stored", HTTP_NOTFOUND_404))
              }
            }
            case KO(k) => KO(Error(k, HTTP_SERVER_ERROR_500))
          }
        } match {
          case Success(s) => OK(APIResponse("", HTTP_OK_200))
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}

