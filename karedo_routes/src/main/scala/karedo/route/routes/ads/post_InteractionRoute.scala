package karedo.route.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp, UserInteraction}
import karedo.route.common.KaredoJsonHelpers
import karedo.route.routes.KaredoRoute
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.DbCollections
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

object post_InteractionRoute extends KaredoRoute
  with post_InteractionActor {

  def route = {
    Route {

      // POST /account/{account_id}/ad/interaction
      path("account" / Segment / "ad" / "interaction") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              post {
                entity(as[post_InteractionRequest]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, request)
                    }
                    )
                }
              }
          }

      }
    }
  }
}

trait post_InteractionActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[post_InteractionActor])

  def exec(accountId: String, deviceId: Option[String], request: post_InteractionRequest): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = request.session_id
    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, Some(sessionId), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          Future {
            request.entries.foreach(x => {
              val iu = UserInteraction(account_id = accountId, interaction = x)

              dbUserInteract.insertNew(iu)
            })
          }

          OK(APIResponse("", code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}

