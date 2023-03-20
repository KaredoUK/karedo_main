package karedo.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp}
import karedo.routes.KaredoRoute
import karedo.util.{KO, OK, Result, _}
import org.slf4j.LoggerFactory
import spray.json._

object get_MessagesRoute extends KaredoRoute
  with get_MessagesActor {

  def route = {
    Route {

      // GET /account/{{account_id}}/messages?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "messages") {
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

trait get_MessagesActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[get_MessagesActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false, respondAnyway = true)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        else {
          val acc = uAccount.get
          val list = dbUserMessages.getMessages(acc.id)

          val ret = list.toJson.toString
          OK(APIResponse(ret, code))

        }


      }
    )
  }

}