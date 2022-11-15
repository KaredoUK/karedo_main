package karedo.rtb.dsp

import java.util.concurrent.Executors

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.Uri._
import akka.http.scaladsl.model._

import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.scaladsl.{Sink, Source}
import karedo.entity.UserPrefData
import karedo.rtb.dsp.HttpDispatcher.{httpDispatcher, httpsDispatcher}
import karedo.rtb.model.AdModel._
import karedo.rtb.model.BidJsonImplicits
import karedo.rtb.model.BidRequestCommon.{Device, User}
import karedo.rtb.model.BidRequestModel_2_2_1.BidRequest
import karedo.rtb.model.BidResponseModelCommon.BidResponse
import karedo.rtb.util._
import karedo.rtb.util.DeviceMakes
import karedo.util.Util.now

import scala.concurrent.{Await, ExecutionContext, Future}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import HttpDispatcher._

/**
  * Created by charaj on 16/12/2016.
  */
class SmaatoDspBidDispatcher (config: DspBidDispatcherConfig)
  extends DspBidDispather
    with DeviceMakes
    with RtbConstants
    with LoggingSupport
    with BidJsonImplicits {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

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
        .map(x => getAdUnitFromResponse(x.get)).flatten

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

  def getAdUnitFromResponse(response:SmaatoAdResponse): List[AdUnit] = {
    logger.debug(marker, s"IN: ${class_name}.getAdUnitFromBidResponse. Response is ${response.toString}" )

    val smaatoResponse = scalaxb.fromXML[generated.Response](xml.XML.loadString(response.content))

    val adUnit:List[Option[AdUnit]] = smaatoResponse.status match {
      case "success" => {
        val sout:List[Option[AdUnit]] = smaatoResponse.ads.get.ad.map(a => {
          val aout:Option[AdUnit] = getAdFromSmaato(a) match {
            case Some(_ad) => {
              Some(
                  AdUnit(
                  ad_type = ad_type_IMAGE
                  , ad_id = a.id.get
                  , impid = a.campaign.get.id
                  , ad = _ad
                  , price = if(a.pricing.isDefined) a.pricing.get.value.toDouble else 2.0
                  , ad_domain = None
                  , iurl = None
                  , nurl = None
                  , cid = a.campaign.get.id
                  , crid = a.id.get
                  , w = banner_w
                  , h = banner_h
                )
              )
            }
            case None => None
          }

          aout
        }).toList

        sout
      }
      case "error" => {
        val err = smaatoResponse.error.get
        logger.error(s"SMAATO ERROR: ${err.code} -> ${err.desc}")
        List(None)
      }
      case _ => {
        logger.error(s"SMAATO ERROR: Received ${smaatoResponse.status} from SMAATO. Expecting either success or error")
        List(None)
      }
    }

    logger.debug(marker, s"IN: ${class_name}.getAdUnitFromBidResponse. AdUnit Returned is ${adUnit}" )

    adUnit.filter(_.isDefined).map(_.get)
  }

  def getAdFromSmaato(a: generated.Ad): Option[Ad] = {
    a.action match {
      case Some(_ad) => {
        Some(
          Ad(
            imp_url = _ad.source.get,
            click_url = _ad.target.get,
            ad_text = a.adtext.getOrElse(""),
            ad_source = if(a.brand.isDefined) Some(a.brand.get.name) else None ,
            duration = None,
            h  = a.height.map(_.toInt),
            w = a.width.map(_.toInt),
            beacons = if(a.beacons.isDefined) a.beacons.get.beacon.map(b => List(Beacon(b))) else None
          )
        )
      }
      case None => None
    }
  }

  val apiver = "502"

  val adSpaceID = "130211537"
  val publisherID = "1100028064"

  val nsupport = "title,txt,image"

  val format_image = "img"
  val format_natiev = "native"

  val formatstrict = "false"
  val dimension = "xxlarge"
  val dimensionstrict = "true"

  val iosadtracking = "true"
  val googlednt = "false"

  val coppa = "0"

  val responseFormat = "xml"

  def buildRequest(seqId: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): Map[String, String] = {

    import scala.collection._

    val ts = now
    val suffix:String = s"${ts.getMillis}"
    val _id:String = s"${user.id}-${suffix}"

    val iabCats:List[String] = (for((k, v) <- iabCatMap if(Math.random() < v.value)) yield v.name).toList
    val kws = iabCats.map(_.replaceAll("&", ",").replaceAll(" ", "")).reduce((l,r) => s"${l},${r}")

    val params:mutable.Map[String, String] = mutable.Map()

    params += ("apiver" -> apiver)
    params += ("adspace" -> adSpaceID )
    params += ("pub" -> publisherID )

    if( device.ip.isDefined ) params += ("devip" -> device.ip.get)

    params += ("ref" -> site_page)

    if(device.ua.isDefined) params += ("device" -> device.ua.get)

    params += ("nsupport" -> nsupport)
    params += ("nver" -> "1")

    params += ("format" -> format_image) //@TODO: Add Native here.
    params += ("formatstrict" -> formatstrict)

    params += ("dimension" -> dimension)
    params += ("dimensionstrict" -> dimensionstrict)

    params += ("width" -> banner_w.toString)
    params += ("height" -> banner_h.toString)

    if(deviceRequest.ifa.isDefined) {
      make match {
        case DEV_TYPE_ANDROID => {
          params += ("googleadid" -> deviceRequest.ifa.get)
        }
        case DEV_TYPE_IOS => {
          params += ("iosadid" -> deviceRequest.ifa.get)
        }
      }
    }

    make match {
      case DEV_TYPE_ANDROID => {
        params += ("googlednt" -> googlednt)
      }
      case DEV_TYPE_IOS => {
        params += ("iosadtracking" -> iosadtracking)
      }
    }

    params += ("response" -> responseFormat)

    params += ("coppa" -> coppa)

    if (user.yob.isDefined) params += ("age" -> (now.getYear - user.yob.get + 1).toString)
    if (user.gender.isDefined) params += ("gender" -> user.gender.get) //@TODO: May not be right. Check. Should be m/f

    device.geo match {
      case Some(geo) => {
        if(geo.lat.isDefined && geo.lon.isDefined) params += ("gps" -> s"${geo.lat.get},${geo.lon.get}")
        if(geo.zip.isDefined) params += ("zip" -> geo.zip.get)
        if(geo.region.isDefined) params += ("regions" -> geo.region.get)
      }
      case _ =>
    }

    if (device.make.isDefined) params += ("devicemake" -> device.make.get)
    if (device.model.isDefined) params += ("devicemodel" -> device.model.get)

    params += ("kws" -> kws)

    val out = params.toList.map(v => s"${v._1}=${v._2}").reduce((l,r) => s"${l}&${r}")

    logger.debug(s"PARAMETERS: ${out}")

    params.toMap
  }

  def sendRequest(params: Map[String, String], deviceRequest: DeviceRequest ): Option[SmaatoAdResponse] = {
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
      http_headers += headers.RawHeader("x-mh-X-Forwarded-For", xffOption.get)
      http_headers += headers.RawHeader("X-Forwarded-For", xffOption.get)
    }

    if( deviceRequest.ua.isDefined ) http_headers += headers.RawHeader("x-mh-User-Agent", deviceRequest.ua.get)

    val uri_path = config.endpoint

//    def deserialize[T](r: HttpResponse)(implicit um: Unmarshaller[ResponseEntity, T]): Future[Option[T]] = {
//      r.status match {
//        case OK => {
//          logger.debug(s"${config.name}: Successful Response 200 OK")
//          Unmarshal(r.entity).to[T] map Some.apply
//        }
//        case _ => {
//          logger.error(s"${config.name}: Failed Response: ${r}")
//          Future(None)
//        }
//      }
//    }

    def responseEntityToSmaato(r: HttpResponse): SmaatoAdResponse = {
      logger.debug(marker, s"IN: ${class_name}.responseEntityToSmaato. Response: ${r}" )

      SmaatoAdResponse(
        headers = r.headers,
        content = r.entity.dataBytes.toString
      )
    }

    // @TODO: Change Below.
    def apiCall: Future[HttpResponse] = {
      val uri = Uri(uri_path).withQuery(Query(params))

      logger.info(s"URI: ${uri}")

      val out_request = HttpRequest(
        GET,
        uri = uri,
        headers = http_headers.toList
      )

      logger.info(s"Request (${config.name}) => ${out_request}")

      val dispatcher = config.scheme match {
        case HTTP => httpDispatcher
        case HTTPS => httpsDispatcher
      }

      dispatcher.singleRequest(out_request)
    }

    try {
      val responseFuture = apiCall

      val response = Await.result(responseFuture, rtb_max_wait)

      logger.info(s"${config.name}: Response from Exchange: " + response)

      Some(responseEntityToSmaato(response))
    } catch {
      case e: Exception => {
        logger.error(s"${config.name}: Error Sending Bid Request to [${config.endpoint}] failed.\n Params:\n${params}", e)
        None
      }
    }
  }

  case class SmaatoAdResponse
  (
    headers: Seq[HttpHeader],
    content: String
  )
}
