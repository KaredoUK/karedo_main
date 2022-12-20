package karedo.actors.intent

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


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