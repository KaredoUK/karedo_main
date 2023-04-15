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
object post_ValidateEmailRoute extends KaredoRoute
  with post_ValidateEmailActor {

  def route = {
    Route {
      path("validate" / "email") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            post {
              entity(as[post_ValidateEmailRequest]) {
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

trait post_ValidateEmailActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{
  override val logger = LoggerFactory.getLogger(classOf[post_ValidateEmailActor])

  def exec(deviceId:Option[String], request:post_ValidateEmailRequest): Result[Error, APIResponse] = {

    val application_id = request.application_id
    val account_id = request.account_id
    val session_id = request.session_id

    authenticate(account_id, deviceId, application_id, Some(session_id), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          uAccount.get.email.filter(x => x.address == request.email) match {
            case mh :: mt => {
              if(mh.valid) OK(APIResponse(ValidateBooleanResponse(true).toJson.toString.toString, code))
              else OK(APIResponse(ValidateBooleanResponse(false).toJson.toString.toString, code))
            }
            case Nil => OK(APIResponse(ValidateBooleanResponse(false).toJson.toString.toString, code))
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}
