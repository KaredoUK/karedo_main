package karedo.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity
import karedo.entity._
import spray.json.DefaultJsonProtocol

/**
  * Created by pakkio on 10/9/16.
  */
import karedo.util.DateTimeJsonHelper._

trait KaredoJsonHelpers extends DefaultJsonProtocol
with SprayJsonSupport {
  implicit val jsonChannel = jsonFormat2(Channel)
  implicit val jsonBacon = jsonFormat1(entity.Beacon)
  implicit val jsonImage = jsonFormat5(ImageAd)
  implicit val jsonVideo = jsonFormat6(VideoAd)
  implicit val jsonAd = jsonFormat13(Ad)
  implicit val jsonAds = jsonFormat2(Ads)

  implicit val jsonAction = jsonFormat2(Action)
  implicit val jsonMessage = jsonFormat6(UserMessages)


  case class Kar166Request(entries: List[UserAd] = List())
  implicit val jsonUserAd = jsonFormat10(UserAd)
  implicit val jsonKar166Request = jsonFormat1(Kar166Request)
}
