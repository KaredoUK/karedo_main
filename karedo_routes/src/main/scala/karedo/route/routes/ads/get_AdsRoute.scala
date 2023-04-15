package karedo.route.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.common.akka.DefaultActorSystem
import karedo.persist.entity._
import karedo.route.routes.KaredoRoute
import karedo.rtb.actor._
import karedo.rtb.model.AdModel.{DeviceRequest, adUnit, _}
import karedo.common.result.{KO, OK, Result}
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import org.slf4j.LoggerFactory
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by pakkio on 10/3/16.
  */
object get_AdsRoute extends KaredoRoute
  with get_AdsActor {

  def route = {
    Route {
      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}
      path("account" / Segment / "ads") {
        accountId =>
          extractRequest { request =>
            optionalHeaderValueByName("X_Identification") {
              deviceId =>
                optionalHeaderValueByName("User-Agent") {
                  ua =>
                    optionalHeaderValueByName("X-Forwarded-For") {
                      xff =>
                        extractClientIP {
                          ip =>
                            get {
                              parameters('p, 's ?, 'c.as[Int], 'lat.as[Double].?, 'lon.as[Double].?, 'ifa ?, 'make ?, 'model ?, 'os ?, 'osv ?, 'did ?, 'dpid ?, 'mac ?, 'cc ?, 'lmt.as[Int].?) {
                                (applicationId, sessionId, adCount, lat, lon, ifa, make, model, os, osv, did, dpid, mac, cc, lmt) =>
                                  doCall({
                                    System.setProperty("java.net.preferIPv4Stack" , "true")

                                    val devObj = DeviceRequest(
                                      ua = ua,
                                      xff = xff,
                                      ifa = ifa,
                                      deviceType = Some(getDeviceType(make, model)),
                                      ip = if( ip.getAddress.isPresent) Some(ip.getAddress.get.getHostAddress) else None,
                                      make = make,
                                      model = model,
                                      os = os,
                                      osv = osv,
                                      did = did,
                                      dpid = dpid,
                                      mac = mac,
                                      lat = lat,
                                      lon = lon,
                                      country = cc,
                                      lmt = lmt,
                                      src_headers = request.headers.map(e => e.name() -> e.value()).toMap
                                    )
                                    exec(accountId, deviceId, applicationId, sessionId, adCount, devObj)
                                  }
                                  )
                              }
                            }
                        }
                    }

                }
            }
          }
      }
    }
  }
}


trait get_AdsActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with DefaultActorSystem {


  override val logger = LoggerFactory.getLogger(classOf[get_AdsActor])

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
          val price_USD_per_1m = ad.price_USD_per_1k * 1000 // per USD $c
          price_USD_per_1m * USER_PERCENT
        }

        def getAdsFor(application: UserApp, uAcc: UserAccount): Result[Error, String] = {
          val userId = uAcc.id
          val adsReceived = adActor.getAds(AdRequest(userId = userId, count = adCount, device = devObj))

          for {
            x <- 0 to adsRepeat
          } yield Future {
            adActor.getAds(AdRequest(userId = userId, count = adCount, device = devObj))
          }

          val pointsGained = adsReceived.map(
            ad => computePoints(ad)
          ).sum.toInt

          val account_hash = storeAccountHash(accountId) match {
            case OK(h) => h
            case KO(h) => h
          }

          def getMagicUrl(url: Option[String], execute: Boolean): Option[String] = {
            if(execute && url.isDefined) storeUrlMagic(url.get, None) match {
              case OK(url_code) => Some(s"${url_magic_share_base}/nrm?u=${url_code}&v=${account_hash}")
              case KO(_) => url
            } else url
          }

          val adsBack = adsReceived.map {
            adUnit =>
              val impify = (Math.random() <= adsImpProb)
              adUnit.copy(
                ad_type = if (impify) ad_type_TEXT else adUnit.ad_type,
                ad = adUnit.ad.copy (
                  imp_url = if (impify) getMagicUrl(adUnit.ad.imp_url, adsMarkUrlImp) else Some(""),
                  click_url = getMagicUrl(Some(adUnit.ad.click_url), adsMarkUrlClick).get
                )
            )
          }


          val uUserKaredos = dbUserKaredos.addKaredos(uAcc.id, pointsGained)

          if (uUserKaredos.isKO) KO(Error(s"Cant add karedos to user because of ${uUserKaredos.err}"))
          else {
            val uKaredoChange = dbKaredoChange.insertNew(
              KaredoChange(accountId = uAcc.id, trans_type = TRANS_TYPE_CREATED, trans_info = "Sum of Karedos from Ads",
                trans_currency = "KAR", karedos = pointsGained))

            if (uKaredoChange.isKO) KO(Error(s"Cant track karedo history in karedochange because of ${uKaredoChange.err}"))
            else {
              OK(
                AdResponse(
                  JsonAccountIfNotTemp(uAcc),
                  adsBack.size,
                  adsBack
                ).toJson.toString)
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


