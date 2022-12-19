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
  with KaredoConstants
{
  override val logger = LoggerFactory.getLogger(classOf[Kar189_postProfile_actor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: Kar189Req): Result[Error, APIResponse] = {

    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[String, UserProfile]] {
          val user_account = uAccount.get

          dbUserProfile.find(user_account.id) match {
            case OK(userProfile) => {
              val profile = userProfile.copy(
                gender = if(request.profile.gender.isDefined) request.profile.gender else userProfile.gender,
                first_name = if(request.profile.first_name.isDefined) request.profile.first_name else userProfile.first_name,
                last_name = if(request.profile.last_name.isDefined) request.profile.last_name else userProfile.last_name,
                yob = if(request.profile.yob.isDefined) request.profile.yob else userProfile.yob,
                kids = if(request.profile.kids.isDefined) request.profile.kids else userProfile.kids,
                income = if(request.profile.income.isDefined) request.profile.income else userProfile.income,
                postcode = if(request.profile.postcode.isDefined) request.profile.postcode else userProfile.postcode,
                location = if(request.profile.location.isDefined) request.profile.location else userProfile.location,
                opt_in = if(request.profile.opt_in.isDefined) request.profile.opt_in else userProfile.opt_in,
                third_party = if(request.profile.third_party.isDefined) request.profile.third_party else userProfile.third_party,
                ts_updated = now
              )

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