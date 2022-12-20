package karedo.entity

import java.util.UUID

import karedo.entity.dao._
import salat.annotations._



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
   hint: Double = 0.0
) extends Keyable[String]

// add implementation if you need special functionalities
trait DbAds extends DbMongoDAO_Casbah[String, AdUnitType]


