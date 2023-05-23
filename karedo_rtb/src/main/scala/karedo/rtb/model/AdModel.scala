package karedo.rtb.model


import akka.http.scaladsl.model.HttpRequest
import karedo.rtb.util.{DEV_TYPE_ANDROID, DEV_TYPE_IOS, DeviceMake}
import spray.json._


/**
  * Created by crajah on 03/09/2016.
  */
object AdModel extends DefaultJsonProtocol {

  case class DeviceRequest
  (
    ua : Option[String] = None
    , xff: Option[String] = None
    , ifa : Option[String] = None
    , deviceType: Option[Int] = None
    , ip: Option[String] = None
    , make: Option[String] = None
    , model: Option[String] = None
    , os: Option[String] = None
    , osv: Option[String] = None
    , did: Option[String] = None
    , dpid: Option[String] = None
    , mac: Option[String] = None
    , lat: Option[Double] = None
    , lon: Option[Double] = None
    , country: Option[String] = Some("GB")
    , lmt: Option[Int] = Some(0) // Limit Ad Tracking
    , src_headers: Map[String, String] = Map()
  )

  case class AdRequest
  ( userId: String
    , count: Int
    , device: DeviceRequest
  )

  case class AdResponse(ad_count:Int, ads: List[AdUnit])

  val ad_type_IMAGE = "IMAGE"
  val ad_type_VIDEO = "VIDEO"
  val ad_type_NATIVE = "NATIVE"
  val ad_type_TEXT = "TEXT"
  val ad_type_ADMOB = "ADMOB"

  case class Beacon(beacon: String)

  case class Ad(
                 imp_url: Option[String],
                 click_url: String,
                 ad_text: String,
                 ad_source: Option[String],
                 duration: Option[Int] = None,
                 h: Option[Int] = None,
                 w: Option[Int] = None,
                 beacons: Option[List[Beacon]] = None
               )

  case class AdUnit(
                     ad_type: String,
                     ad_app_id: Option[String] = None,
                     ad_unit_id: Option[String] = None,
                     ad_id: String,
                     impid: String,
                     ad: Ad,
                     price_USD_per_1k: Double, // In USD eCPM
                     ad_domain: Option[List[String]],
                     iurl: Option[String],
                     nurl: Option[String],
                     cid: String,
                     crid: String,
                     w: Int,
                     h: Int,
                     hint: Double = 0.0
                   )


  implicit val beacon = jsonFormat1(Beacon)
  implicit val ad = jsonFormat8(Ad)
  implicit val adUnit = jsonFormat15(AdUnit)

  implicit val adResponse:RootJsonFormat[AdResponse] = jsonFormat2(AdResponse)

  implicit val json_DeviceRequest = jsonFormat17(DeviceRequest)
  implicit val adRequest:RootJsonFormat[AdRequest] = jsonFormat3(AdRequest)

}