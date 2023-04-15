package karedo.route.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{IntentUnit, UserAccount, UserApp, UserIntent}
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.util._
import org.slf4j.LoggerFactory
import karedo.common.result.{KO, OK, Result}

/**
  * Created by pakkio on 10/3/16.
  */
object put_IntentRoute extends KaredoRoute
  with put_IntentActor {

  def route = {
    Route {

      // PUT /account/{{account_id}}/intent
      path("account" / Segment / "intent" ) {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              put {
                entity(as[IntentUpdateRequest]) {
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

trait put_IntentActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
{

  override val logger = LoggerFactory.getLogger(classOf[put_IntentActor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: IntentUpdateRequest
          ): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        uAccount match {
          case KO(e) => KO(Error(s"internal error ${e}"))
          case OK(acc) => {
            dbUserIntent.find(acc.id) match {
              case OK(intent) => {
                val newIntent = IntentUnit(getNewRandomID,
                  request.intent.why, request.intent.what, request.intent.when, request.intent.where, now)

                val newUserIntent = intent.copy(intents = intent.intents ++ List(newIntent))

                dbUserIntent.update(newUserIntent) match {
                  case OK(_) => OK(APIResponse("", code))
                  case KO(error) => KO(Error(s"Internal error ${error}"))
                }
              }
              case KO(error) => {
                val newIntent = IntentUnit(getNewRandomID,
                  request.intent.why, request.intent.what, request.intent.when, request.intent.where, now)
                val userIntent = UserIntent(acc.id, List(newIntent) )

                dbUserIntent.insertNew(userIntent) match {
                  case OK(x) => OK(APIResponse("", code))
                  case KO(error) => KO(Error(s"Internal error ${error}"))
                }
              }
            }
          }
        }
      }
    )
  }
}