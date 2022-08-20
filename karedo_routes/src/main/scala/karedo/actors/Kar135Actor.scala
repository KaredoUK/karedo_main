package karedo.actors

import karedo.entity.{UserAccount, UserApp}
import karedo.entity.dao.{KO, OK, Result}
import karedo.util.KaredoJsonHelpers
import org.slf4j.LoggerFactory
import spray.json.{JsNumber, JsObject, JsString}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar135Actor extends KaredoCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar135Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, f = getPoints, allowCreation = false)
  }

  def getPoints(uapp: Result[String, UserApp], uAcc: Result[String, UserAccount], code: Int): Result[Error, APIResponse] = {
    if (uAcc.isKO) KO(Error(s"internal error ${uAcc.err}"))
    else {
      val acc = uAcc.get
      val upoints = dbUserKaredos.find(acc.id)
      if (upoints.isKO) KO(Error(s"internal error ${upoints.err}"))
      val prefix = if (!acc.temp)
        JsObject("accountId" -> JsString(acc.id)).toString + ", "
      else ""

      val ret = prefix + JsObject("app_karedos" -> JsNumber(upoints.get.karedos)).toString
      OK(APIResponse(ret, code))
    }
  }

}
