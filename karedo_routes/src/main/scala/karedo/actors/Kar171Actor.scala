package karedo.actors

import java.util.UUID

import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar171Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {

  override val logger = LoggerFactory.getLogger(classOf[Kar171Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
           deviceId: Option[String],
           request: Kar170Req
          ): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)


    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

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