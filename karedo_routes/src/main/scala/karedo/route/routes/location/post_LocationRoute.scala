package karedo.route.routes.location

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.common.result.{KO, OK, Result}
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import karedo.route.routes.KaredoRoute
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object post_LocationRoute extends KaredoRoute
  with post_LocationActor
{
  def route = Route {
    Route {
      path("location" ) {
        optionalHeaderValueByName(AUTH_HEADER_NAME) {
          deviceId =>
            post {
              entity(as[post_Location]) {
                request =>
                  doCall({
                    exec(deviceId, request)
                  })
              }
            }
        }
      }
    }

  }
}

trait post_LocationActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
{
  override val logger = LoggerFactory.getLogger(classOf[post_LocationActor])

  def exec(deviceId: Option[String], request: post_Location): Result[Error, APIResponse] = {
    val accountId = request.header.account_id
    val applicationId = request.header.application_id
    val sessionId = Some(request.header.session_id)

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[Error, APIResponse]] {
          OK(APIResponse(""))
        } match {
          case Success(s) => s
          case Failure(e) => MAKE_THROWN_ERROR(e)
        }
      }
    )
  }
}