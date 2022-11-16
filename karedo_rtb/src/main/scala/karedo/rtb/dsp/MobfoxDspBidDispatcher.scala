package karedo.rtb.dsp

import java.util.concurrent.Executors

import akka.http.scaladsl._
import model._
import Uri._
import HttpMethods._
import MediaTypes._
import StatusCodes._
import unmarshalling.{Unmarshal, Unmarshaller}

import akka.stream.scaladsl.{Sink, Source}
import karedo.entity.UserPrefData
import karedo.rtb.model.AdModel._
import karedo.rtb.model.BidRequestCommon.{Device, User}
import karedo.rtb.util.{DeviceMakes, _}
import karedo.util.Util.now
import spray.json._

import scala.concurrent.{Await, ExecutionContext, Future}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._


/**
  * Created by charaj on 16/12/2016.
  */
class MobfoxDspBidDispatcher(config: DspBidDispatcherConfig)
  extends DspBidDispather(config)
    with DeviceMakes
    with RtbConstants
    with LoggingSupport
    with ResponseNativeImplicits {

  import ResponseNativeModel._

  implicit val ecLocal: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  lazy val class_name = this.getClass.getName

  override def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): List[AdUnit] = {
    logger.debug(marker, s"IN: ${class_name}.getAds. Count is ${count}, User is: ${user}, Device is ${device}")

    try {
      val fSeq = Future.sequence(
        (0 until count).toList.map(
          x ⇒ Future(
            sendRequest(
              buildRequest(x, user, device, iabCatMap, make, deviceRequest)
              , deviceRequest
            )
          )
        )
      )

      val ads: List[AdUnit] = Await.result(fSeq, rtb_max_wait)
        .filter(x => {
          x match {
            case Some(_) => true
            case None => false
          }
        })
        .map(x => {
          getAdUnitFromResponse(x.get)
        }).flatten

      logger.debug(marker, s"IN: ${class_name}.getAds. Ads Returned: ${ads}" )

      ads
    } catch {
      case e: Exception => {
        println(s"Dispatchers not found" + "\n" + e.getMessage + "\n" + e.getStackTrace.foldLeft("")((z, b) => z + b.toString + "\n"))
        logger.error("Unable to Send Bid Request", e)
        List()
      }
    }
  }

  def getAdUnitFromResponse(response:HttpAdResponse): List[AdUnit] = {
    logger.debug(marker, s"IN: ${class_name}.getAdUnitFromBidResponse. Response is ${response.toString}" )

    val nativeResponse = JsonParser(response.content).convertTo[NativeResponse]

    val adUnit:Option[AdUnit] = nativeResponse.error match {
      case Some(e) => {
        logger.error("Mobfox Error Response : " + e)
        None
      }
      case None => {
        nativeResponse.native match {
          case None => None
          case Some(native) => {
            var icon:Option[Img] = None
            var main:Option[Img] = None
            var title:Option[Title] = None
            var desc:Option[Data] = None

            var icon_id = java.util.UUID.randomUUID().toString
            var main_id = java.util.UUID.randomUUID().toString
            var title_id = java.util.UUID.randomUUID().toString
            var desc_id = java.util.UUID.randomUUID().toString

            native.assets.foreach(a => {
              a.$type match {
                case "icon" => {
                  icon = a.img
                  icon_id = a.id
                }
                case "main" => {
                  main = a.img
                  main_id = a.id
                }
                case "title" => {
                  title = a.title
                  title_id = a.id
                }
                case "desc" => {
                  desc = a.data
                  desc_id = a.id
                }
              }
            })

            val adUnit = AdUnit(
              ad_type = ad_type_NATIVE
              , ad_id = main_id
              , impid = main_id
              , ad = Ad
              (
                imp_url = main.getOrElse(icon.getOrElse(Img("", banner_w, banner_h))).url,
                click_url = native.link.url,
                ad_text = title.getOrElse(Title("")).text,
                ad_source = Some("mobfox"),
                duration = None,
                h = Some(main.getOrElse(Img("", banner_w, banner_h)).h),
                w = Some(main.getOrElse(Img("", banner_w, banner_h)).h),
                beacons = nativeResponse.imptrackers.map(_.map(t => Beacon(t)))
              )
              , price_USD_per_1k = (nativeResponse.priceCPM.getOrElse(config.price_cpm) * (1 - config.comm_percent))
              , ad_domain = None
              , iurl = None
              , nurl = None
              , cid = main_id
              , crid = main_id
              , w = banner_w
              , h = banner_h
            )

            Some(adUnit)
          }
        }
      }
    }

    logger.debug(marker, s"IN: ${class_name}.getAdUnitFromBidResponse. AdUnit Returned is ${adUnit}" )

    adUnit match {
      case None => List()
      case Some(a) => List(a)
    }
  }

  val apiver = "502"

  val adSpaceID = "130211537"
  val publisherID = "1100028064"
  val inventoryID = "415dbe4be9e47c5e2779ed03ba943ae5"

  val nsupport = "title,txt,image"

  val format_image = "img"
  val format_natiev = "native"

  val formatstrict = "1" // 0 - False, 1 - True
  val dimension = "xxlarge"
  val dimensionstrict = "true"

  val iosadtracking = "true"
  val googlednt = "false"

  val coppa = "0"

  val responseFormat = "xml"

  val interstitial = "1"
  val jscript_support = "0"

  val allow_mr = "1"

  def buildRequest(seqId: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): Map[String, String] = {

    import scala.collection._

    val ts = now
    val suffix:String = s"${ts.getMillis}"
    val _id:String = s"${user.id}-${suffix}"

    val iabCats:List[String] = (for((k, v) <- iabCatMap if(Math.random() < v.value)) yield v.name).toList
    val kws = iabCats.map(_.replaceAll("&", ",").replaceAll(" ", "")).reduce((l,r) => s"${l},${r}")

    val params:mutable.Map[String, String] = mutable.Map()

    params += ("rt" -> "api")
    params += ("r_type" -> "native")
    params += ("s" -> inventoryID)

    params += ("r_floor" -> floor_price.toString)

    params += ("sub_name" -> app_name)
    params += ("sub_domain" -> app_domain)
    params += ("sub_bundle_id" -> app_bundle)
    params += ("sub_storeurl" -> (make match {
      case DEV_TYPE_IOS => app_storeurl_ios
      case DEV_TYPE_ANDROID => app_storeurl_android
    }))

    if(device.ip.isDefined ) params += ("i" -> device.ip.get)
    if(device.ua.isDefined) params += ("u" -> java.net.URLEncoder.encode(device.ua.get, java.nio.charset.StandardCharsets.UTF_8.toString()))

    params += ("n_adunit" -> "in_ad")
    params += ("n_ver" -> "1.1")

    params += ("n_layout" -> "content_wall")
    params += ("n_context" -> "content")
    params += ("n_plcmttype" -> "in_feed")

    params += ("n_img_large_req" -> "1")
    params += ("n_img_large_w" -> banner_w.toString)
    params += ("n_img_large_h" -> banner_h.toString)

    params += ("n_title_req" -> "1")


    params += ("allow_mr" -> allow_mr)

    params += ("adspace_strict" -> formatstrict)
    params += ("adspace_width" -> banner_w.toString)
    params += ("adspace_height" -> banner_h.toString)

    params += ("imp_instl" -> interstitial)

    params += ("dev_js" -> jscript_support)

    if(deviceRequest.ifa.isDefined) {
      make match {
        case DEV_TYPE_ANDROID => {
          params += ("o_andadvid" -> deviceRequest.ifa.get)
        }
        case DEV_TYPE_IOS => {
          params += ("o_iosadvid" -> deviceRequest.ifa.get)
        }
      }
    }

    make match {
      case DEV_TYPE_ANDROID => {
        params += ("dev_dnt" -> googlednt)
      }
      case DEV_TYPE_IOS => {
        params += ("dev_lmt" -> iosadtracking)
      }
    }

    if (device.make.isDefined) params += ("devicemake" -> device.make.get)
    if (device.model.isDefined) params += ("devicemodel" -> device.model.get)

    if (user.yob.isDefined) params += ("demo_age" -> (now.getYear - user.yob.get + 1).toString)
    if (user.gender.isDefined) params += ("demo_gender" -> user.gender.get) //@TODO: May not be right. Check. Should be m/f

    device.geo match {
      case Some(geo) => {
        if(geo.lat.isDefined && geo.lon.isDefined) {
          params += ("longitude" -> s"${geo.lon.get}")
          params += ("latitude" -> s"${geo.lat.get}")
        }
        if(geo.zip.isDefined) params += ("zip" -> geo.zip.get)
        if(geo.region.isDefined) params += ("regions" -> geo.region.get)
      }
      case _ =>
    }

    params += ("demo_keywords" -> kws)

    val out = params.toList.map(v => s"${v._1}=${v._2}").reduce((l,r) => s"${l}&${r}")

    logger.debug(s"PARAMETERS: ${out}")

    params.toMap
  }

  def sendRequest(params: Map[String, String], deviceRequest: DeviceRequest ): Option[HttpAdResponse] = {
    logger.debug(marker, s"IN: ${class_name}.sendRequest. Request: ${params}" )

    import scala.collection._


    val http_headers:mutable.MutableList[HttpHeader] = mutable.MutableList()

    // Make the headers
    val xffOption = deviceRequest.xff match {
      case Some(x) if deviceRequest.ip.isDefined => Some(x + ", " + deviceRequest.ip.get)
      case Some(x) => Some(x)
      case None if deviceRequest.ip.isDefined => Some(deviceRequest.ip.get)
      case None => None
    }

    if( xffOption.isDefined ) {
      http_headers += headers.RawHeader("X-Forwarded-For", xffOption.get)
    }

    val uri_path = config.endpoint

    try {
      val responseFuture = singleRequestCall(uri_path, params, http_headers.toList)

      val response = Await.result(responseFuture, rtb_max_wait)

      logger.info(s"${config.name}: Response from Exchange: " + response)

      Some(responseEntityToHttpAdResponse(response))
    } catch {
      case e: Exception => {
        logger.error(s"${config.name}: Error Sending Bid Request to [${config.endpoint}] failed.\n Params:\n${params}", e)
        None
      }
    }
  }
}

object ResponseNativeModel  {

  val type_icon = "icon"
  val type_main = "main"
  val type_title = "title"
  val type_desc = "desc"
  val type_rating = "rating"
  val type_ctatext = "ctatext"
  val type_sponsored = "sponsored"

  case class NativeResponse
  (
    error: Option[String] = None
    , native: Option[Native]
    , imptrackers: Option[List[String]]
    , priceCPM: Option[Double] = None
  )

  case class Native
  (
    link: Link,
    assets: List[Asset]
  )

  case class Link
  (
    url: String
  )

  case class Asset
  (
    id: String,
    required: Int,
    $type: String,
    img: Option[Img],
    title: Option[Title],
    data: Option[Data]
  )

  case class Img
  (
    url: String,
    w: Int,
    h: Int
  )

  case class Title
  (
    text: String
  )

  case class Data
  (
    value: String
  )

}

trait ResponseNativeImplicits extends DefaultJsonProtocol {
  import ResponseNativeModel._

  implicit val json_Data = jsonFormat1(Data)
  implicit val json_Title = jsonFormat1(Title)
  implicit val json_Img = jsonFormat3(Img)
  implicit val json_Asset = jsonFormat6(Asset)
  implicit val json_Link = jsonFormat1(Link)
  implicit val json_Native = jsonFormat2(Native)
  implicit val json_NativeResponse:RootJsonFormat[NativeResponse] = jsonFormat4(NativeResponse)
}
