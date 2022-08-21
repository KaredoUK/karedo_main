package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.entity.dao.Util.now
import karedo.rtb.model.AdRequest


case class Beacon(beacon: String="")

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

// add implementation if you need special functionalities
trait DbAds extends DbMongoDAO[String, Ads] {
  // preloads some values associated to accountId: accountid
  def preload(applicationId: String, count: Int) = {

    deleteAll()

    def element(i: Int): Ad = Ad(price=10,imageAd = Some(ImageAd(s"Image $i")))

    val l = (for (i <- 1 to count) yield element(i)).toList
    insertNew(Ads(applicationId, l))


  }
}


