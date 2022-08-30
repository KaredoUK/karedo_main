package karedo.actors

import karedo.entity.{UserAccount, UserApp, UserProfile}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar189Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar189Actor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: Kar189Req): Result[Error, APIResponse] = {

    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get

          val profileResult = dbUserProfile.find(acc.id)

          if( profileResult.isKO) {
            // Create a new profile.
            val profile = UserProfile(acc.id, request.profile.gender, request.profile.first_name,
              request.profile.last_name, request.profile.yob, request.profile.kids, request.profile.income,
              request.profile.location, request.profile.opt_in, request.profile.third_party, Some(now), now)

            val res = dbUserProfile.insertNew(profile)

            if( res.isOK) {
              OK(APIResponse("", code))
            } else {
              KO(Error(s"Internal Error ${res.err}"))
            }
          } else {
            val profile = UserProfile(acc.id, request.profile.gender, request.profile.first_name,
              request.profile.last_name, request.profile.yob, request.profile.kids, request.profile.income,
              request.profile.location, request.profile.opt_in, request.profile.third_party, profileResult.get.ts_created , now)

            val res = dbUserProfile.update(profile)

            if( res.isOK) {
              OK(APIResponse("", code))
            } else {
              KO(Error(s"Internal Error ${res.err}"))
            }
          }
        }
      }
    )
  }
}