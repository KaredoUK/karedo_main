package karedo.actors

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity
import karedo.entity._
import karedo.entity.dao.{KO, OK, Result}
import karedo.util.{DefaultActorSystem, KaredoJsonHelpers}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spray.json._
import akka.actor.{ActorSystem, Props}
import karedo.rtb.actor._
import karedo.rtb.model.AdModel._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{Await, Future}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar134Actor
  extends KaredoCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with DefaultActorSystem {


  override val logger = LoggerFactory.getLogger(classOf[Kar134Actor])

  val KAREDO_REVENUE_PERCENT = 0.80

  val USER_PERCENT =   .40

  val adActor = system.actorOf(Props[AdActor])

  // exec will be moved to proper actor (or stream in business logic layer)
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
            implicit val duration: Timeout = 1 second
            val adBack = (adActor ? AdRequest(uAcc.id, count)).mapTo[Result[Error, AdResponse]]

            val rAds = Await.result(adBack, 1 second)

            if (rAds.isKO) KO(Error(s"cant find application id in dbads ${rAds.err}"))
            else {
              val list = rAds.get.ads
              val pointsGained = list.map(
                ad => computePoints(ad)
              ).sum.toInt
              val uUserKaredos = dbUserKaredos.addKaredos(uAcc.id, pointsGained)
              if (uUserKaredos.isKO) KO(s"Cant add karedos to user because of ${uUserKaredos.err}")
              val uKaredoChange = dbKaredoChange.insertNew(
                KaredoChange(accountId = uAcc.id, trans_type = "/ads", trans_info = "receiving ads", trans_currency = "karedos", karedos = pointsGained))
              if (uKaredoChange.isKO) KO(s"Cant track karedo history in karedochange because of ${uKaredoChange.err}")
              OK(JsonAccountIfNotTemp(uAcc) + list.toJson.toString)
            }
          } catch {
            case e: Exception => KO(Error(e.toString))
          }

        }

        // STARTS HERE
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

    ret
  }


}


