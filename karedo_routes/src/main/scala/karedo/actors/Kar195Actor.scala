package karedo.actors

import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar195Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar195Actor])

  def exec(accountId: String,
           deviceId: Option[String],
           request: Kar195Req): Result[Error, APIResponse] = {

    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get

          val prefListRes = dbPrefs.ids

          if( prefListRes.isKO ) {
            KO(Error(s"Internal Error ${prefListRes.err}"))
          }
          val prefList = prefListRes.get
          val prefMapLocked = prefList.map (x => x -> 0.5) (collection.breakOut): Map[String, Double]
          val prefMap = collection.mutable.Map() ++ prefMapLocked

          request.prefs.foreach(x => prefMap(x._1) = x._2)

          val prefs = UserPrefs(acc.id,
            Map(prefMap.toSeq: _*), Some(now), now )

          val prefRes = dbUserPrefs.find(acc.id)

          if( prefRes.isKO) {
            // Create a new Prefs

            val res = dbUserPrefs.insertNew(prefs)

            if( res.isOK) {
              OK(APIResponse("", code))
            } else {
              KO(Error(s"Internal Error ${res.err}"))
            }
          } else {
            // Update old
            val res = dbUserPrefs.update(prefs)

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