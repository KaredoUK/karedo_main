package jobs

import javax.inject.Singleton

import akka.actor.Actor
import common.DbHelper
import karedo.persist.entity.AdUnitType

import scala.concurrent.duration._
import karedo.rtb.dsp.FeedLoader
import karedo.common.result.{KO, OK}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

/**
  * Created by charaj on 11/02/2017.
  */
@Singleton
class AdLoadActor extends Actor with DbHelper {
  val logger = LoggerFactory.getLogger(this.getClass)

  context.system.scheduler.schedule(
    0 seconds, 1 hour, self, "load"
  )


  def receive = {
    case "load" => loadFeeds
  }

  def loadFeeds = {
    println("Ad Laod Triggered")
    logger.debug("Loading Feeds & Ads")

    def saveAd(ad: AdUnitType): AdUnitType = {
      dbAds.insertNew(ad) match {
        case OK(_) => ad
        case KO(_) => dbAds.update(ad) match {
          case OK(_) => ad
          case KO(_) => ad
        }
      }
    }

    dbFeeds.findAll() match {
      case OK(feeds) => {
        val ads = FeedLoader.getAdsFromFeeds(feeds, 1.0, Some(saveAd _))

//        dbAds.insertMany(ads).foreach {
//          _ match {
//            case OK(r) => logger.debug(s"Ads saved to DB\n${r}")
//            case KO(f) => logger.error(s"Failed - Write Ads to DB: ${f}")
//          }
//        }
      }
      case KO(f) => logger.error(s"Failed - Get Feeds from DB: ${f}")
    }
  }
}

