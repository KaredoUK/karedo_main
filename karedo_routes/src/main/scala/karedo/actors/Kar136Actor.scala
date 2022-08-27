package karedo.actors

import karedo.util.{KO, OK, Result}
import karedo.entity.{UserAccount, UserApp, UserMessages}
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar136Actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar136Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get
          val list = dbUserMessages.getMessages(acc.id)

          val ret = JsonAccountIfNotTemp(acc) + list.toJson.toString
          OK(APIResponse(ret, code))

        }


      }
    )
  }

}