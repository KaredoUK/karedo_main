package karedo.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity
import karedo.entity.{Channel, ImageAd, UserAd, VideoAd}
import spray.json.DefaultJsonProtocol

/**
  * Created by pakkio on 10/9/16.
  */
import karedo.util.DateTimeJsonHelper._

trait KaredoJsonHelpers extends DefaultJsonProtocol
with SprayJsonSupport  {
  implicit val jsonChannel = jsonFormat2(Channel)
  implicit val jsonBacon = jsonFormat1(entity.Beacon)
  implicit val jsonImage = jsonFormat5(ImageAd)
  implicit val jsonVideo = jsonFormat6(VideoAd)
  implicit val jsonUserAd = jsonFormat17(UserAd)
}
