package karedo.actors.profile

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp, UserProfile}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import scala.util.{Try, Success, Failure}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar189_postProfile_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar189_postProfile_actor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: Kar189Req): Result[Error, APIResponse] = {

    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[String, UserProfile]] {
          val user_account = uAccount.get

          dbUserProfile.find(user_account.id) match {
            case OK(userProfile) => {
              val profile = UserProfile(user_account.id, request.profile.gender, request.profile.first_name,
                request.profile.last_name, request.profile.yob, request.profile.kids, request.profile.income, request.profile.postcode,
                request.profile.location, request.profile.opt_in, request.profile.third_party, userProfile.ts_created , now)

              dbUserProfile.update(profile)
            }
            case KO(_) => {
              val profile = UserProfile(id = user_account.id, gender = request.profile.gender,
                first_name = request.profile.first_name, last_name = request.profile.last_name,
                yob = request.profile.yob, kids = request.profile.kids, income = request.profile.income,
                postcode = request.profile.postcode,
                location = request.profile.location, opt_in = request.profile.opt_in,
                third_party = request.profile.third_party, ts_created = now, ts_updated = now)

              dbUserProfile.insertNew(profile)
            }
          }
        } match {
          case Success(s) => OK(APIResponse("", HTTP_OK_200))
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}