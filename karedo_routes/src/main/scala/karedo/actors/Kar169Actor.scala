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


trait Kar169Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {

  override val logger = LoggerFactory.getLogger(classOf[Kar169Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           intentId: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nintentId: $intentId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get

          val intentRes = dbUserIntent.find(acc.id)

          if( intentRes.isKO ) {
            // create a new one
            val userIntent = UserIntent(acc.id, List() )

            val uIsertRes = dbUserIntent.insertNew(userIntent)

            if( uIsertRes.isOK) {
              // Send the new stuff
              OK(APIResponse(userIntent.toJson.toString))
            } else {
              KO(Error(s"Internal error ${uIsertRes.err}"))
            }
          } else {
            // get what's there
            OK(APIResponse(intentRes.get.toJson.toString))
          }
        }
      }
    )
  }
}