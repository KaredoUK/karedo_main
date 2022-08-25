package karedo.actors

import karedo.entity.{DbCollections, UserAccount, UserApp}
import karedo.util.{KO, KaredoJsonHelpers, OK, Result}
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar166Actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar166Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String, sessionId: Option[String], deviceId: Option[String], request: Kar166Request): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    val applicationId = if(request.entries.size>0) request.entries(0).application_id else ""
    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get
          val results = request.entries.foreach(ad => dbUserAd.insertNew(ad))

          val ret = JsonAccountIfNotTemp(acc)
          OK(APIResponse(ret, code))

        }


      }
    )
  }

}