package karedo.routes.profile

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.routes.KaredoRoute
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._


/**
  * Created by pakkio on 10/3/16.
  */
object get_ProfileRoute extends KaredoRoute
  with get_ProfileActor {

  def route = {
    Route {

      // GET /account/{{account_id}}/profile?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "profile") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              get {
                parameters('p, 's ?) {
                  (applicationId, sessionId) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId)
                    }
                    )
                }
              }
          }
      }
    }
  }
}

trait get_ProfileActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[get_ProfileActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

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