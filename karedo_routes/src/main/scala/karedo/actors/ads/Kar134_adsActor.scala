package karedo.actors.ads

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.rtb.actor._
import karedo.rtb.model.AdModel._
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar134_adsActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with DefaultActorSystem {


  override val logger = LoggerFactory.getLogger(classOf[Kar134_adsActor])

  val adActor = actor_system.actorOf(Props[AdActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           adCount: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nadCount: $adCount")

    val ret = authenticate(accountId, deviceId, applicationId, sessionId) {
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        // 1 karedo for each ad returned :)
        def computePoints(ad: AdUnit): Double = {
          ad.price * KAREDO_REVENUE_PERCENT * USER_PERCENT
        }

        def getAdsFor(application: UserApp, uAcc: UserAccount): Result[Error, String] = {

          // @TODO: Change this to call the karedo_rtb AdActor.
          // @TODO: Remember to return Result object form AdActor
          // val rAds = dbAds.find(application.id)

          val count = adCount.getOrElse("10").toInt


          try {
            implicit val duration: Timeout = 5 second
            val adBack = (adActor ? AdRequest(uAcc.id, count)).mapTo[Result[Error, AdResponse]]

            val rAds = Await.result(adBack, 10 second)

            if (rAds.isKO) KO(Error(s"cant find application id in dbads ${rAds.err}"))
            else {
              val list = rAds.get.ads
              val pointsGained = list.map(
                ad => computePoints(ad)
              ).sum.toInt
              val uUserKaredos = dbUserKaredos.addKaredos(uAcc.id, pointsGained)

              if (uUserKaredos.isKO) KO(s"Cant add karedos to user because of ${uUserKaredos.err}")

              val uKaredoChange = dbKaredoChange.insertNew(
                KaredoChange(accountId = uAcc.id, trans_type = TRANS_TYPE_CREATED, trans_info = "Sum of Karedos from Ads",
                  trans_currency = "KAR", karedos = pointsGained))

              if (uKaredoChange.isKO) KO(s"Cant track karedo history in karedochange because of ${uKaredoChange.err}")

              OK(Kar134Res(JsonAccountIfNotTemp(uAcc), rAds.get.ad_count, rAds.get.ads).toJson.toString)

              //              OK( JsonAccountIfNotTemp(uAcc) + rAds.get.toJson.toString)
            }
          } catch {
            case e: Exception => KO(Error(e.toString))
          }

        }

        // STARTS HERE
        if (uAccount.isKO) {
          KO(Error(s"Internal error ${uAccount.err}",code))
        } else {
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
      }
    }

    ret
  }


}


