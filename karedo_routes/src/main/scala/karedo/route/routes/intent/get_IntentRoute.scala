package karedo.route.routes.intent

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors._
import karedo.route.routes._
import karedo.route.util._
import org.slf4j.LoggerFactory
import karedo.persist.entity._
import spray.json._

/**
  * Created by pakkio on 10/3/16.
  */
object get_IntentRoute extends KaredoRoute
  with get_IntentActor {

  def route = {
    Route {

      // GET /account/{{account_id}}/intent/{{intent_id}}?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "intent" / Segment) {
        (accountId, intentId) =>
            optionalHeaderValueByName("X_Identification") {
              deviceId =>
                get {
                  parameters('p, 's ?) {
                    (applicationId, sessionId) =>
                      doCall({
                        exec(accountId, deviceId, applicationId, sessionId,
                          (if (intentId != null && ! intentId.isEmpty && ! intentId.equals("0")) Some(intentId) else None ) )
                      }
                    )
                }
              }
          }
      }
    }
  }
}

trait get_IntentActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants {

  override val logger = LoggerFactory.getLogger(classOf[get_IntentActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           intentId: Option[String]): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nintentId: $intentId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get

          dbUserIntent.find(acc.id) match {
            case OK(intent) => {
              intentId match {
                case None => OK(APIResponse(intent.toJson.toString))
                case Some(intent_id) => {
                  val found_intent = intent.intents.filter(i => i.intent_id.equals(intent_id))

                  val new_intent = intent.copy(intents = found_intent)
                  OK(APIResponse(new_intent.toJson.toString, code))
                }
              }
            }
            case KO(_) => {
              val userIntent = UserIntent(acc.id, List() )

              dbUserIntent.insertNew(userIntent) match {
                case OK(x) => OK(APIResponse(userIntent.toJson.toString, code))
                case KO(error) => KO(Error(s"Internal error ${error}"))
              }
            }
          }
        }
      }
    )
  }
}