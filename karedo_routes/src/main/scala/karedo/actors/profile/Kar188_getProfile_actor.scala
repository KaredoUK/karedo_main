package karedo.actors.profile

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp, UserProfile}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar188_getProfile_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar188_getProfile_actor])

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
            val profile = UserProfile(
              id = acc.id, gender = None, first_name =  None, last_name =  None,
              yob = None, kids = None, income = None, postcode = None, location = Some(true), opt_in = Some(true), third_party = Some(true),
              now, now)
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