package karedo.route.routes.prefs

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp, UserPrefData, UserPrefs}
import karedo.route.routes.KaredoRoute
import karedo.common.misc.Util.now
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import karedo.common.result.{KO, OK, Result}

/**
  * Created by pakkio on 10/3/16.
  */
object post_PrefsRoute extends KaredoRoute
  with post_PrefsActor {

  def route = {
    Route {

      // POST /account/{{account_id}}/prefs
      path("account" / Segment / "prefs") {
        accountId =>
          optionalHeaderValueByName(AUTH_HEADER_NAME) {
            deviceId =>
              post {
                entity(as[post_PrefsRequest]) {
                  request =>
                    doCall({
                      exec(accountId, deviceId, request)
                    }
                    )
                }
              }
          }
      }
    }
  }
}

trait post_PrefsActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[post_PrefsActor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: post_PrefsRequest): Result[Error, APIResponse] = {

    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

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
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}