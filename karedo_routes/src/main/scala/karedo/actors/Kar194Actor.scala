package karedo.actors

import karedo.entity.{DbCollections, UserAccount, UserApp, UserPrefs, UserPref}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar194Actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar194Actor])

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

          val prefsRes = dbUserPrefs.find(acc.id)

          if( prefsRes.isKO) {
            // Create a new Prefs
            val prefListRes = dbPrefs.ids

            if( prefListRes.isOK ) {
              val prefList = prefListRes.get

              val prefs = UserPrefs(acc.id,
                prefList.map( UserPref(_, 0.5) ), Some(now), now )

              val res = dbUserPrefs.insertNew(prefs)
              if( res.isOK) {
                val ret = JsonAccountIfNotTemp(acc) + prefs.toJson.toString
                OK(APIResponse(ret, code))
              } else {
                KO(Error(s"Internal Error ${res.err}"))
              }
            } else {
              KO(Error(s"Internal Error ${prefListRes.err}"))
            }
          } else {
            // Send the profile we have
            val ret = JsonAccountIfNotTemp(acc) + prefsRes.get.toJson.toString
            OK(APIResponse(ret, code))
          }
        }
      }
    )
  }
}