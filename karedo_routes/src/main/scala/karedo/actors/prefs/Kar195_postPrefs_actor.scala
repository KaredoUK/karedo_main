package karedo.actors.prefs

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import scala.util.{Try, Success, Failure}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar195_postPrefs_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar195_postPrefs_actor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: Kar195Req): Result[Error, APIResponse] = {

    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try [Result[Error, APIResponse]] {
          dbUserPrefs.find(accountId) match {
            case OK(userPrefs) => {
              val prefMapOrig:Map[String, UserPrefData] = userPrefs.prefs
              val prefMapDefault:Map[String, UserPrefData] = getDefaultPrefMap

              val prefMapLocked:Map[String, UserPrefData] = if( prefMapOrig.size < prefMapDefault.size ) prefMapDefault else Map()

              val prefMap:collection.mutable.Map[String, UserPrefData] = collection.mutable.Map() ++ prefMapLocked

              prefMapOrig.foreach(x => prefMap(x._1) = prefMapOrig(x._1))

              request.prefs.foreach(x => prefMap(x._1) = prefMap(x._1).copy(value = x._2))

              val prefs = UserPrefs(accountId,
                Map(prefMap.toSeq: _*), Some(now), now )

              dbUserPrefs.update(prefs)

              OK(APIResponse("", code))
            }
            case KO(_) => {
              val prefListRes = dbPrefs.ids
              val prefMapLocked = getDefaultPrefMap
              val prefMap = collection.mutable.Map() ++ prefMapLocked

              request.prefs.foreach(x => prefMap(x._1) = prefMap(x._1).copy(value = x._2))

              val prefs = UserPrefs(accountId,
                Map(prefMap.toSeq: _*), Some(now), now )

              dbUserPrefs.insertNew(prefs)

              OK(APIResponse("", code))
            }
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}