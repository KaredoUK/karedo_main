package karedo.actors

import karedo.entity.{UserAccount, UserApp, UserProfile}
import karedo.util._
import org.slf4j.LoggerFactory
import karedo.util.Util.now
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar188Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar188Actor])

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

          val profileResult = dbUserProfile.find(acc.id)

          if( profileResult.isKO) {
            // Create a new profile.
            val profile = UserProfile(acc.id, Some(GENDER_FEMALE), None, None, None, None, None, Some(true), Some(true), Some(true), Some(now), now)
            val res = dbUserProfile.insertNew(profile)
            if( res.isOK) {
              val ret = profile.toJson.toString
              OK(APIResponse(ret, code))
            } else {
              KO(Error(s"Internal Error ${res.err}"))
            }
          } else {
            // Send the profile we have
            val ret = profileResult.get.toJson.toString
            OK(APIResponse(ret, code))
          }
        }
      }
    )
  }
}