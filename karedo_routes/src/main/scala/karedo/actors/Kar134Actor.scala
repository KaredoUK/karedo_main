package karedo.actors

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity
import karedo.entity._
import karedo.entity.dao.{KO, OK, Result}
import karedo.util.KaredoJsonHelpers
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar134Actor
  extends KaredoCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {


  override val logger = LoggerFactory.getLogger(classOf[Kar134Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           adCount: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nadCount: $adCount")

    authenticate(accountId,deviceId,applicationId,sessionId,f = getAds)


  }

  def getAds(uapp: Result[String, UserApp], uAccount: Result[String,UserAccount], code: Int): Result[Error, APIResponse] = {
    if (uapp.isOK) {
      val app = uapp.get
      //val uAccount = dbUserAccount.getById(app.account_id)
      if (uAccount.isKO) KO(Error(s"Application maps to an invalid account ${app.account_id}"))
      else {
        val acct = uAccount.get
        val uAds = getAdsFor(app, acct)
        if (uAds.isOK) {
          val ads = uAds.get
          OK(APIResponse(ads.toString, code))
        } else KO(Error(s"Can't get ads because of ${uAds.err}"))
      }
    } else KO(Error(s"application cant be found because of ${uapp.err}"))
  }

  // 1 karedo for each ad returned :)
  def computePoints(ad: UserAd): Int = {
    1
  }

  def getAdsFor(application: UserApp, uAcc: UserAccount): Result[String, String] = {

    OK {
      val list = dbUserAd.getAdsForApplication(application.id)
      val pointsGained = list.map(
        ad => computePoints(ad)
      ).sum
      val uUserKaredos = dbUserKaredos.addKaredos(uAcc.id, pointsGained)
      if (uUserKaredos.isKO) KO(s"Cant add karedos to user because of ${uUserKaredos.err}")
      val uKaredoChange = dbKaredoChange.insertNew(
        KaredoChange(accountId = uAcc.id, trans_type = "/ads", trans_info = "receiving ads", trans_currency = "karedos", karedos = pointsGained))
      if (uKaredoChange.isKO) KO(s"Cant track karedo history in karedochange because of ${uKaredoChange.err}")
      val prefix = if (!uAcc.temp)
        JsObject("accountId" -> JsString(uAcc.id)).toString + ", "
      else ""
      prefix + list.toJson.toString
    }
  }


}


