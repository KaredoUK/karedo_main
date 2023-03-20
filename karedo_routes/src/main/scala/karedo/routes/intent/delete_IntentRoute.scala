package karedo.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp}
import karedo.routes.KaredoRoute
import karedo.util._
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/3/16.
  */
object delete_IntentRoute extends KaredoRoute
  with delete_IntentActor {

  def route = {
    Route {

      // DELETE /account/{{account_id}}/intent/{{intent_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              delete {
                entity(as[delete_IntentRequest]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, intentId, request)
                    }
                    )
                }
              }
          }
      }
    }
  }
}

trait delete_IntentActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants {

  override val logger = LoggerFactory.getLogger(classOf[delete_IntentActor])

  def exec(accountId: String,
           deviceId: Option[String],
           intentId: String,
           request: delete_IntentRequest
          ): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nintentId: $intentId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        uAccount match {
          case KO(e) => KO(Error(s"internal error ${e}"))
          case OK(acc) => {
            dbUserIntent.find(acc.id) match {
              case OK(intent) => {
                intent.intents.filter(i => i.intent_id.equals(intentId)) match {
                  case Nil => KO(Error(s"$intentId is not found", 404))
                  case _ => {
                    val restIntentList = intent.intents.filter(i => ! i.intent_id.equals(intentId))

                    val newUserIntent = intent.copy(intents = restIntentList)

                    dbUserIntent.update(newUserIntent) match {
                      case OK(_) => OK(APIResponse("", code))
                      case KO(error) => KO(Error(s"Internal error ${error}"))
                    }
                  }
                }
              }
              case KO(error) => {
                KO(Error(s"Internal error ${error}"))
              }
            }
          }
        }
      }
    )
  }
}