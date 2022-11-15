package karedo.actors.ads

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.rtb.actor._
import karedo.rtb.model.AdModel._
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._
import scala.util.{Try, Success, Failure}

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

  val adActor = new AdActor

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           adCount: Int,
           devObj: DeviceRequest): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nadCount: $adCount")

    val ret = authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = true, respondAnyway = true) {
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        // 1 karedo for each ad returned :)
        def computePoints(ad: AdUnit): Double = {
          ad.price * KAREDO_REVENUE_PERCENT * USER_PERCENT
        }

        def getAdsFor(application: UserApp, uAcc: UserAccount): Result[Error, String] = {
          val userId = uAcc.id
          val adsBack = adActor.getAds(AdRequest(userId = userId, count = adCount, device = devObj))

          val pointsGained = adsBack.map(
            ad => computePoints(ad)
          ).sum.toInt

          val uUserKaredos = dbUserKaredos.addKaredos(uAcc.id, pointsGained)

          if (uUserKaredos.isKO) KO(Error(s"Cant add karedos to user because of ${uUserKaredos.err}"))
          else {
            val uKaredoChange = dbKaredoChange.insertNew(
              KaredoChange(accountId = uAcc.id, trans_type = TRANS_TYPE_CREATED, trans_info = "Sum of Karedos from Ads",
                trans_currency = "KAR", karedos = pointsGained))

            if (uKaredoChange.isKO) KO(Error(s"Cant track karedo history in karedochange because of ${uKaredoChange.err}"))
            else {
              OK(Kar134Res(JsonAccountIfNotTemp(uAcc), adsBack.size, adsBack).toJson.toString)
            }
          }
        }

        // STARTS HERE
        Try [Result[Error, APIResponse]] {
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
                  OK(APIResponse(msg = ads.toString, code = code))
                } else KO(Error(s"Can't get ads because of ${uAds.err}"))
              }
            } else KO(Error(s"application cant be found because of ${uapp.err}"))

          }
        } match {
          case Success(s) => s
          case Failure(f) => KO(Error("", HTTP_SERVER_ERROR_500))
        }
      }
    }

    ret
  }


}


