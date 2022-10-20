package karedo.rtb.dsp

import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import karedo.entity.UserPrefData
import karedo.rtb.model.AdModel._
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.model.BidResponseModelCommon._
import karedo.rtb.model.BidRequestModel_2_2_1._
import karedo.rtb.model.BidJsonImplicits
import karedo.rtb.util._

import scala.concurrent.{Await, ExecutionContext, Future}
import karedo.util.Util.newUUID
import karedo.util.Util.now
import org.slf4j.MarkerFactory

/**
  * Created by crajah on 28/11/2016.
  */
class ORTB2_2_1DspBidDispatcher(config: DspBidDispatcherConfig)
  extends DspBidDispather
    with DeviceMakes
    with RtbConstants
    with LoggingSupport
    with BidJsonImplicits {

  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())


  override def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake): List[AdUnit] = {
    logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.getAds. Count is ${count}, User is: ${user}, Device is ${device}" )

    try {
      val fSeq = Future.sequence(
        (0 until count).toList.map(
          x ⇒ Future(
            sendBidRequest(
              buildBidRequest(x, user, device, iabCatMap, make)
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
        .map(x => getAdUnitFromBidResponse(x.get)).flatten

      logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.getAds. Ads Returned: ${ads}" )

      ads
    } catch {
      case e: Exception => {
        println(s"Dispatchers not found" + "\n" + e.getMessage + "\n" + e.getStackTrace.foldLeft("")((z, b) => z + b.toString + "\n"))
        logger.error("Unable to Send Bid Request", e)
        List()
      }
    }
  }

  def getAdUnitFromBidResponse(response:BidResponse): List[AdUnit] = {
    logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.getAdUnitFromBidResponse. Response is ${response.toJson.toString}" )

    val adUnit = response.seatbid.map(sb => sb.bid.map(b => {
      AdUnit(
        ad_type = ad_type_IMAGE
        , ad_id = b.adid
        , impid = b.impid
        , ad = getAdFromBid(b, response.id, sb.seat)
        , price = b.price
        , ad_domain = b.adomain
        , iurl = b.iurl
        , nurl = b.nurl
        , cid = b.cid
        , crid = b.crid
        , w = b.w
        , h = b.h
      )
    })).flatten

    logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.getAdUnitFromBidResponse. AdUnit Returned is ${adUnit}" )

    adUnit
  }

  def getAdFromBid(orig_bid: Bid, response_id:String, seat_id: String): Ad = {
    logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.getAdFromBid. Orig Bid is ${orig_bid.toJson.toString}, Response ID: ${response_id}, Seat ID: ${seat_id}" )

    import scala.util.{Try, Success, Failure}

    def getUrlPair(ad_markup: String): (String, String) = {
      logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.getAdFromBid.getUrlPair. Ad Markup: ${ad_markup}" )

      import scala.xml._

      val ns = XML.loadString(ad_markup)

      val click_url = (ns \ "a" \ "@src").toString
      val imp_url = (ns \ "a" \ "img" \ "@src").toString

      (imp_url, click_url)
    }

    Try [Ad] {
      val bid = substituteUrlMacrosInBidResponse(orig_bid, response_id, seat_id)

      val ad_markup = bid.adm match {
        case Some(adm) => adm
        case None => bid.nurl match {
          case Some(nurl) => nurl
          case None => throw new Error(s"Ad Markup not found in both adm <${bid.adm}> and nurl <${bid.nurl}>")
        }
      }

      val urlPair = getUrlPair(ad_markup)

      Ad(
        imp_url = urlPair._1
        , click_url = urlPair._2
        , ad_text = ""
        , ad_source = bid.adomain match {case Some(add) => Some(add.head) case None => None}
        , duration = None
        , h = Some(bid.h)
        , w = Some(bid.w)
        , beacons = None
      )
    } match {
      case Success(ad) => ad
      case _ => {
        val u = ("http://karedo.co.uk/ads/c1.jpg",
          "https://www.washingtonpost.com/news/answer-sheet/wp/2015/03/26/no-finlands-schools-arent-giving-up-traditional-subjects-heres-what-the-reforms-will-really-do/",
          "No, Finland isn’t ditching traditional school subjects. Here’s what’s really happening.", Some("Washington Post"))
        Ad( u._1, u._2, u._3, u._4, None, Some(250), Some(300), None)
      }
    }
  }

  def substituteUrlMacrosInBidResponse(bid:Bid, response_id:String, seat_id: String): Bid = {
    def macroReplace(in:Option[String]) : Option[String] = {
      val out = in match {
        case Some(str) => {
          Some(Map(
            ("${AUCTION_ID}", response_id)
            , ("${AUCTION_BID_ID}", seat_id)
            , ("${AUCTION_IMP_ID}", bid.impid)
            , ("${AUCTION_SEAT_ID}", seat_id)
            , ("${AUCTION_AD_ID}", bid.adid)
            , ("${AUCTION_PRICE}", bid.price.toString)
          ).foldLeft(str){case (s, (k, v)) => s.replaceAll(k, v)})
        }
        case None => None
      }

      logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.substituteUrlMacrosInBidResponse.macroReplace. In: ${in}, Out: ${out}" )

      out
    }

    bid.copy(
      adm = macroReplace(bid.adm)
      , iurl = macroReplace(bid.iurl)
      , nurl = macroReplace(bid.nurl)
    )
  }

  def sendBidRequest(bid:BidRequest): Option[BidResponse] = {
    logger.debug(marker, s"IN: ORTB2_2_1DspBidDispatcher.sendBidRequest. Bid Request: ${bid.toJson.toString}" )

    import akka.http.scaladsl._
    import model._
    import Uri._
    import HttpMethods._
    import HttpProtocols._
    import MediaTypes._
    import HttpCharsets._
    import StatusCodes._
    import unmarshalling.{ Unmarshal, Unmarshaller }
    import akka.stream.scaladsl.{ Source, Sink }

    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

    implicit val actor_system = ActorSystem("rtb")
    implicit val actor_materializer = ActorMaterializer()

    val rtbHeader = headers.RawHeader("x-openrtb-version", "2.2")

    def deserialize[T](r: HttpResponse)(implicit um: Unmarshaller[ResponseEntity, T]): Future[Option[T]] = {
      r.status match {
        case OK => {
          logger.debug(s"${config.name}: Successful Response 200 OK")
          Unmarshal(r.entity).to[T] map Some.apply
        }
        case _ => {
          logger.error(s"${config.name}: Failed Response: ${r}")
          Future(None)
        }
      }
    }

    def apiCall: Future[Option[BidResponse]] = {
      val bid_request = HttpRequest(
        POST,
        uri = Uri(path = Path(config.path)),
        entity = HttpEntity(`application/json`, bid.toJson.toString),
        headers = List(rtbHeader)
      )

      logger.info(s"Bid Reqest (${config.name}) => ${bid_request}")

      val source = Source.single(
        bid_request
      )

      val flow = config.scheme match {
        case HTTP => Http().outgoingConnection(host = config.host, port = config.port).mapAsync(1) { r => deserialize[BidResponse](r) }
        case HTTPS => Http().outgoingConnectionHttps(host = config.host, port = config.port).mapAsync(1) { r => deserialize[BidResponse](r)
        }
      }

      source.via(flow).runWith(Sink.head)
    }


    try {
//      val responseFuture:Future[Option[BidResponse]] =
//        Http().singleRequest(
//          HttpRequest(
//            POST,
//            uri = config.endpoint,
//            entity = HttpEntity(`application/json`, bid.toJson.toString),
//            headers = List(rtbHeader)
//          )
//        ).map[Option[BidResponse]]{r => r.status match {
//          case OK => Unmarshal(r.entity).to[BidResponse] map Some.apply
//          case _ => Future(None)
//        }}

      val responseFuture:Future[Option[BidResponse]] = apiCall

      val response = Await.result(responseFuture, rtb_max_wait)

      logger.info(s"${config.name}: Response from Exchange: " + response)

      response
    } catch {
      case e: Exception => {
        logger.error(s"${config.name}: Error Sending Bid Request to [${config.endpoint}] failed.\nBid Request:\n${bid.toJson.toString}", e)
        None
      }
    }
  }

  def buildBidRequest(seqId: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake): BidRequest = {
    val ts = now
    val suffix:String = s"${ts.getMillis}"
    val _id:String = s"${user.id}-${suffix}"

    val iabCats:List[String] = (for((k, v) <- iabCatMap if(Math.random() < v.value)) yield k).toList

    BidRequest(
      id = _id
      , imp = getImps(seqId, _id)
      , app = Some(getApp(iabCats, make))
      , user = user
      , device = device
      , bcat = Some(bid_bcat)
      , tmax = Some(bid_tmax)
    )
  }

  def getId = newUUID

  def getImps(seqId: Int, _id: String): List[Imp] = {
    List(Imp(id = s"${_id}", banner = getBanner(seqId), secure = Some(secure_ad)))
  }

  def getBanner(seqId: Int): Banner = {
    Banner(
      w = banner_w
      , h = banner_h
      , id = seqId
      , pos = Some(banner_pos)
      , btype = Some(banner_btype)
    )
  }

  def getApp(iabCats:List[String], deviceType: DeviceMake):App = {
    App(
      id = app_id
      , name = Some(app_name)
      , domain = Some(app_domain)
      , bundle = Some(app_bundle)
      , privacypolicy = Some(app_privacypolicy)
      , paid = Some(app_paid)
      , storeurl = Some(deviceType match {case Android => app_storeurl_android case iOS => app_storeurl_ios})
      , cat = Some(iabCats)
      , content = Some(Content(cat = Some(iabCats)))
    )
  }
}
