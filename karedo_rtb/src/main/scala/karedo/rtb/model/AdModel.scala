package karedo.rtb.model

import java.util.UUID


/**
  * Created by crajah on 03/09/2016.
  */
object AdModel {
  import spray.json._
  import DefaultJsonProtocol._


  case class AdRequest(userId: String, count: Int)

  case class AdResponse(ad_count:Int, ads: List[AdUnit])

  val ad_type_IMAGE = "IMAGE"
  val ad_type_VIDEO = "VIDEO"
  val ad_type_NATIVE = "NATIVE"

  case class Beacon(beacon: String)

  case class Ad(
                 imp_url: String,
                 click_url: String,
                 ad_text: String,
                 duration: Option[Int] = None,
                 h: Option[Int] = None,
                 w: Option[Int] = None,
                 beacons: Option[List[Beacon]] = None
               )

  case class AdUnit(
                     ad_type: String,
                     ad_id: String,
                     impid: String,
                     ad: Ad,
                     price: Double, // In USD/M eCPM * 0.8
                     adomain: Option[List[String]],
                     iurl: Option[String],
                     nurl: Option[String],
                     cid: Option[String],
                     crid: Option[String],
                     w: Int,
                     h: Int
                   )


  implicit val beacon = jsonFormat1(Beacon)
  implicit val ad = jsonFormat7(Ad)
  implicit val adUnit = jsonFormat12(AdUnit)

  implicit val adResponse:RootJsonFormat[AdResponse] = jsonFormat2(AdResponse)

  implicit val adRequest:RootJsonFormat[AdRequest] = jsonFormat2(AdRequest)

}