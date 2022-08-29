package karedo.actors

import karedo.util._
import karedo.entity.{ UserAccount, UserApp}
import org.slf4j.LoggerFactory
import spray.json.{JsNumber, JsObject, JsString}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar135Actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar135Actor])

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
          val upoints = dbUserKaredos.find(acc.id)
          // @TODO: Add capability of converting karedos to app_karedos

          if (upoints.isKO) KO(Error(s"internal error ${upoints.err}"))
          else {
            val app_karedos:Int = (upoints.get.karedos / APP_KAREDO_CONV).toInt
            val ret = Kar135Res(JsonAccountIfNotTemp(acc), app_karedos).toJson.toString
              OK(APIResponse(ret, code))
          }
        }


      }
    )
  }

}