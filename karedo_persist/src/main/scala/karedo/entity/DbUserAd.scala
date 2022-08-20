package karedo.entity

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.entity.dao.Util.now

case class Channel
(
  channel: String
  , channel_id: String
)

case class Beacon (
  beacon: String
)


case class ImageAd
(
  image_url: String
  , click_url: String = ""
  , h: Int = 0
  , w: Int = 0
  , beacons: List[Beacon] = List()
)
case class VideoAd
(
  video_url: String = ""
  , duration: String = ""
  , click_url: String = ""
  , h: Int = 0
  , w: Int = 0
  , beacons: List[Beacon] = List()
)

case class UserAd
(
  // this is the univoque created by MongoDB
  @Key("_id") id: String = UUID.randomUUID().toString
  , sort: Int = 0 // possibly used to sort this
  , ad_id: String // assigned by Rtb?
  , application_id: String // primary key for extracting ads for this user
  , ad_type: String = "" // VIDEO/IMAGE/NATIVE
  , adm: String = "" // Ad Markup
  , image_ad: Option[ImageAd] = None
  , video_ad: Option[VideoAd] = None
  , addomain: String = ""
  , iurl: String = ""
  , nurl: String = ""
  , cid: String = ""
  , crid: String = ""
  , w: Int = 0
  , h: Int = 0
  , channels: List[Channel] = List()
  , ts: DateTime = now

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbUserAd extends DbMongoDAO[String, UserAd] {
  // preloads some values associated to accountId: accountid
  def preload(applId: String, num: Int) = {

    def add(index: Int) = {

      val image=ImageAd(image_url = s"imageurl $index")
      val video=VideoAd(video_url = s"videourl $index")
      val inserted = insertNew(
        UserAd(ad_id = index.toString,
        application_id = applId,
        addomain = "domain.com", cid = "cid", crid = "crid"
        ,channels = List(Channel("channel","CH01"), Channel("channel2","CH02"))
      ))
    }

    deleteAll()
    for (i <- 1 to num) add(i)
    10

  }

  def getAdsForApplication(applicationId: String) = {
    dao.find(MongoDBObject("application_id" -> applicationId)).toList
  }
}



