package karedo.actors.ads

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp}
import karedo.util.{KO, OK, Result, _}
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar136_messagesActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar136_messagesActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false, respondAnyway = true)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get
          val list = dbUserMessages.getMessages(acc.id)

          val ret = list.toJson.toString
          OK(APIResponse(ret, code))

        }


      }
    )
  }

}