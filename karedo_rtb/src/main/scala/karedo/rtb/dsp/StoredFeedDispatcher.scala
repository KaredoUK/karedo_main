package karedo.rtb.dsp

import karedo.entity.{DbAds, UserPrefData}
import karedo.rtb.model.AdModel.{AdUnit, DeviceRequest}
import karedo.rtb.model.BidJsonImplicits
import karedo.rtb.model.BidRequestCommon.{Device, User}
import karedo.rtb.util.{DeviceMake, LoggingSupport}
import karedo.util.{KO, OK}
import org.slf4j.LoggerFactory


import scala.util.{Failure, Success, Try}

/**
  * Created by charaj on 12/02/2017.
  */
class StoredFeedDispatcher (config: DspBidDispatcherConfig)
  extends DspBidDispather(config)
    with LoggingSupport
    with BidJsonImplicits { self =>

  override val logger = LoggerFactory.getLogger(classOf[StoredFeedDispatcher])

  override def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): List[AdUnit] = {
    logger.info("In Stored Dispatcher")

    Try {
      val dbAds = new DbAds {}

      val allAds = iabCatMap.map { case (k, v) =>
        (k, v, dbAds.findAllbyPref(k, count) match {
          case OK(x) => x
          case KO(_) => List()
        })
      }

      val adsWithCat = allAds.filter(f => ! f._3.isEmpty)

      val sumVals:Double = adsWithCat.foldLeft(0.0)((a,f) => a + f._2.value )

      val iabCount = adsWithCat.map(f => (scala.util.Random.shuffle(f._3).take((f._2.value / sumVals * count).toInt + 1))).flatten.toList

//
//
//      val iabToCount = iabInDb.map(f => (f._1, Math.ceil(f._2.value / sumVals * count).toInt))
//
//      val ads = iabToCount.map { f =>
//        val adIab = dbAds.findAllbyPref(f._1) match {
//          case OK(ads) => ads
//          case KO(_) => List()
//        }
//
//        scala.util.Random.shuffle(adIab).take(f._2)
//      }

      AdMechanic.adUntiTypeToAdUnit(scala.util.Random.shuffle(iabCount).take(count))
    } match {
      case Success(s) => s
      case Failure(f) => {
        logger.error("Failed getting Ads", f)
        List()
      }
    }

  }
}
