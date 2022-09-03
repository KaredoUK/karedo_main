package karedo.actors.ads

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar166_interaction_actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar166_interaction_actor])

  def exec(accountId: String, sessionId: Option[String], deviceId: Option[String], request: Kar166Request): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    val applicationId = if(request.entries.size>0) request.entries(0).application_id else "unknownApplicationId"
    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}",code))
        else {
          val acc = uAccount.get
          val results = request.entries.foreach(ad => dbUserAd.insertNew(ad.copy(ts = now) ))

          OK(APIResponse("", code))

        }


      }
    )
  }

}