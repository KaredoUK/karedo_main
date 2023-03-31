package karedo.persist.entity

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import karedo.persist.entity.dao._
import karedo.route.util.{KO, OK, Result}
import salat.annotations._

import scala.util.{Failure, Success, Try}

import org.joda.time.DateTime




/*

case class ImageAd(
                    image_url: String = "",
                    click_url: String = "",
                    h: Option[Int] = None,
                    w: Option[Int] = None,
                    beacons: Option[List[Beacon]] = None
                  )

case class VideoAd(
                    video_url: String = "",
                    duration: Int = 0,
                    click_url: String = "",
                    h: Option[Int] = None,
                    w: Option[Int] = None,
                    beacons: Option[List[Beacon]] = None
                  )
case class Ads(
              // applicationId as key
              @Key("_id") id: String,
              ads: List[Ad]
              ) extends Keyable[String]

case class Ad(
             ad_id: String = UUID.randomUUID().toString,
                ad_type: String = "IMAGE",
                impid: String = "",
                price: Double = 0, // In USD/M eCPM * 0.8
                imageAd: Option[ImageAd] = None,
                videoAd: Option[VideoAd] = None,
                adomain: Option[List[String]] = None,
                iurl: Option[String] = None,
                nurl: String = "",
                cid: Option[String] = None,
                crid: Option[String] = None,
                w: Int = 0,
                h: Int = 0
              )
*/

case class BeaconType(beacon: String="")

case class AdType
(
  imp_url: String,
  click_url: String,
  ad_text: String,
  ad_source: Option[String],
  duration: Option[Int] = None,
  h: Option[Int] = None,
  w: Option[Int] = None,
  beacons: Option[List[BeaconType]] = None
)


case class AdUnitType
(
  @Key("_id") id: String,
   ad_type: String,
   impid: String,
   ad: AdType,
   price_USD_per_1k: Double, // In USD eCPM
   ad_domain: Option[List[String]],
   iurl: Option[String],
   nurl: Option[String],
   cid: String,
   crid: String,
   w: Int,
   h: Int,
   hint: Double = 0.0,
  prefs: List[String] = List(),
  source: String,
  locale: Option[String] = None,
  pub_date: DateTime
) extends Keyable[String]

// add implementation if you need special functionalities
trait DbAds extends DbMongoDAO_Casbah[String, AdUnitType] {
  def insertMany(adUnitTypes: List[AdUnitType]): List[Result[String, AdUnitType]] = {
    adUnitTypes.map {
      ad =>
        Try {
          dao.insert(ad)
        } match {
          case Success(x) => OK(ad)
          case Failure(error) => KO(error.toString)
        }
    }
  }

  def findAllbyPref(pref: String, limit: Int = 20): Result[String, List[AdUnitType]] = {
    Try[List[AdUnitType]] {
      val query = MongoDBObject("prefs" -> pref)

      dao.find(query)
//        .aggregate(MongoDBObject("$sample" -> limit))
        .limit(limit).toList

//      dao.collection.aggregate(MongoDBObject("$sample" -> limit)).results.toList.asInstanceOf[List[AdUnitType]]
    } match {
      case Success(x) => OK(x)
      case Failure(error) => KO(error.toString)
    }
  }

  def findAllPrefs: Result[String, Set[String]] = {
    Try[Set[String]] {
      val query = MongoDBObject()
      val projs = MongoDBObject("prefs" -> 1)

      dao.find(query, projs).toList.map(_.prefs).flatten.toSet
    } match {
      case Success(x) => OK(x)
      case Failure(error) => KO(error.toString)
    }
  }
}


