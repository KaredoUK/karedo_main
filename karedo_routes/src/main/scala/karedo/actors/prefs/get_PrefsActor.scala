package karedo.actors.prefs

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp, UserPrefData, UserPrefs}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._
import scala.util.{Try, Success, Failure}

/**
  * Created by pakkio on 10/8/16.
  */


trait get_PrefsActor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[get_PrefsActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[Error, APIResponse]] {
          val acc = uAccount.get

          dbUserPrefs.find(acc.id) match {
            case OK(userPrefs) => {
              val ret = PrefsListResponse(sortPrefMap(userPrefs.prefs)).toJson.toString
              OK(APIResponse(ret, code))
            }
            case KO(_) => {
              val prefMap = getDefaultPrefMap

              val prefs = UserPrefs(acc.id,
                prefMap, Some(now), now )

              val res = dbUserPrefs.insertNew(prefs)

              val ret = PrefsListResponse(sortPrefMap(prefMap)).toJson.toString
              OK(APIResponse(ret, code))
            }
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}