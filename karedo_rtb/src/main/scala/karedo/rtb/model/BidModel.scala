package karedo.rtb.model

import spray.json._
import DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport


/**
  * Created by crajah on 11/08/2016.
  *
  * Contains the case classes for OpenRTB versions 2.2.1 & 2.4
  */

object BidRequestCommon extends DefaultJsonProtocol {
  // Site or App => Not both
  case class App
  ( id:String,
    name:Option[String] = Some("karedo"),
    domain:Option[String] = Some("karedo.co.uk"),
    cat:Option[List[String]] = None,
    sectioncat:Option[List[String]] = None,
    pagecat:Option[List[String]] = None,
    ver:Option[String] = Some("1.0"),
    bundle:Option[String] = Some("uk.co.karedo"),
    privacypolicy:Option[Int] = Some(0),
    paid:Option[Int] = Some(0),
    publisher:Option[Publisher] = None,
    content:Option[Content] = None,
    keywords:Option[String] = None,
    storeurl:Option[String] = Some("") //@TODO: Need a URL here.
  )

  case class Content
  ( id:Option[String] = None,
    episode:Option[Int] = None,
    title:Option[String] = None,
    series:Option[String] = None,
    season:Option[String] = None,
    producer:Option[Producer] = None,
    url:Option[String] = None,
    cat:Option[List[String]] = None,
    videoquality:Option[Int] = None,
    context:Option[Int] = None,
    contentrating:Option[String] = None,
    userrating:Option[String] = None,
    qagmediarating:Option[Int] = None,
    keywords:Option[String] = None,
    livestream:Option[Int] = None,
    sourcerelationship:Option[Int] = None,
    len:Option[Int] = None,
    language:Option[String] = None,
    embeddable:Option[Int] = None
  )

  case class Publisher
  ( id:String,
    name:Option[String] = Some("karedo"),
    cat:Option[List[String]] = None,
    domain:Option[String] = Some("karedo.co.uk")
  )

  case class Producer
  ( id:String,
    name:Option[String] = Some("karedo"),
    cat:Option[List[String]] = None,
    domain:Option[String] = Some("karedo.co.uk")
  )

  case class Site
  ( id:String,
    name:Option[String] = Some("karedo"),
    domain:Option[String] = Some("karedo.co.uk"),
    cat:Option[List[String]] = None,
    sectioncat:Option[List[String]] = None,
    pagecat:Option[List[String]] = None,
    page:Option[String] = Some("http://www.karedo.co.uk/main"),
    privacypolicy:Option[Int] = Some(0),
    ref:Option[String] = None,
    search:Option[String] = None,
    mobile:Option[Int] = Some(1), // 2.4 Only
    publisher:Option[Publisher] = None,
    content:Option[Content] = None,
    keywords:Option[String] = None
  )

  case class Data
  ( id:Option[String] = None,
    name:Option[String] = None,
    segment:Option[List[Segment]] = None
  )

  case class Segment
  ( id:Option[String] = None,
    name:Option[String] = None,
    value:Option[String] = None
  )

  case class Regs
  (coppa:Option[Int] = None)

  case class Pmp
  ( private_auction:Option[Int] = None,
    deals:Option[List[Deal]] = None
  )

  case class Deal
  ( id:String,
    bidfloor:Option[Double] = Some(0),
    bidfloorcur:Option[String] = Some("USD"),
    at:Option[Int] = None,
    wseat:Option[List[String]] = None,
    wadomain:Option[List[String]] = None
  )

  case class Geo
  ( lat:Option[Double] = None,                                       // Y
    lon:Option[Double] = None,                                       // Y
    // $type:Int = 1,
    accuracy: Option[Int] = None, // 2.4 Only
    lastfix:Option[Int] = None, // 2.4 Only
    ipservice: Option[Int] = None, // 2.4 Only
    country:Option[String] = Some("GB"),              // ISO 3166-1 Alpha 3
    region:Option[String] = None,                     // ISO 3166-2
    regionfips104:Option[String] = None,              // FIPS 10-4
    metro:Option[String] = None,
    city:Option[String] = None,
    zip:Option[String] = None,                        // Y, postcode
    utoffset:Option[Int] = None // 2.4 Only
  )

  case class User
  ( id:String,
    buyeruid:Option[String] = None,
    yob:Option[Int] = None,
    gender:Option[String] = None,
    keywords:Option[List[String]] = None,
    customdata:Option[String] = None,
    geo:Option[Geo] = None,
    data:Option[List[Data]] = None
  )

  case class Device
  ( ua:Option[String] = None,                                 // Y
    geo:Option[Geo] = None, // 2.4 Only                       // Y
    dnt:Option[Int] = Some(0), // OK ot track.
    lmt:Option[Int] = Some(1), // 2.4 Only
    ip:Option[String] = Some("127.0.0.1"),                    // Y
    // ipv6:Option[String] = None, // 2.2.1 Only
    devicetype:Option[Int] = Some(1),                         // Y, 1 - mobile/tablet, 4 - phone, 5 - tablet
    make:Option[String] = None,                               // Y
    model:Option[String] = None,                              // Y
    os:Option[String] = None,                                 // Y
    osv:Option[String] = None,                                // Y
    hwv:Option[String] = None, // 2.4 Only
    h:Option[Int] = None, // 2.4 Only
    w:Option[Int] = None, // 2.4 Only
    // ppi:Option[Int] = Some(200), // 2.4 Only
    // pxratio:Option[Double] = Some(1.17), // 2.4 Only
    js:Option[Int] = Some(0),
    // geofetch:Option[Int] = Some(0), //2.4 Only
    // flashver:Option[String] = None,
    language:Option[String] = None,
    // carrier:Option[String] = None,
    // connectiontype:Option[Int] = None,
    ifa:Option[String] = None,                                 // N (Apple's IFA or Android's Advertiding ID)
    didsha1:Option[String] = None,                             // IMEI or MEID or ESN
    didmd5:Option[String] = None,
    dpidsha1:Option[String] = None,                            // Android ID or UDID in iOS
    dpidmd5:Option[String] = None,
    macsha1:Option[String] = None,                             // MAC address
    macmd5:Option[String] = None
  )

  implicit  val json_Segment = jsonFormat3(Segment)
  implicit  val json_Data = jsonFormat3(Data)
  implicit  val json_Geo = jsonFormat12(Geo)
  implicit  val json_User = jsonFormat8(User)
  implicit  val json_Device = jsonFormat22(Device)

  implicit  val json_Deal = jsonFormat6(Deal)
  implicit  val json_Pmp = jsonFormat2(Pmp)
  implicit  val json_Regs = jsonFormat1(Regs)
  implicit  val json_Producer = jsonFormat4(Producer)
  implicit  val json_Publisher = jsonFormat4(Publisher)
  implicit  val json_Content = jsonFormat19(Content)
  implicit  val json_Site = jsonFormat14(Site)
  implicit  val json_App = jsonFormat14(App)
}

object BidRequestModel_2_2_1 extends DefaultJsonProtocol  {

  import BidRequestCommon._

  case class BidRequest
  (id:String,
   imp:List[Imp],
   site:Option[Site] = None,
   app:Option[App] = None,
   device:Device,
   user:User,
   at:Option[Int] = Some(2),
   tmax:Option[Int] = Some(250), //@TODO: Set from application.conf
   wseat:Option[List[String]] = None,
   allimps:Option[Int] = None,
   cur:Option[String] = None, //Some("USD"),
   bcat:Option[List[String]] = None, // @TODO: Need some values here.
   badv:Option[List[String]] = None,
   regs:Option[Regs] = None
  )

  case class Imp
  (id:String,
   banner:Banner,
   video:Option[Video] = None,
   displaymanager:Option[String] = None,
   displaymanagerver:Option[String] = None,
   instl:Option[Int] = None,
   tagid:Option[String] = None,
   bidfloor:Option[Double] = None, // @TODO: Need toset some value here.
   bidfloorcur:Option[String] = None,
   secure:Option[Int] = Some(1), // @TODO: What is the right value here?
   iframebuster:Option[List[String]] = None,
   pmp:Option[Pmp] = None
  )

  case class Banner
  ( w:Int = 300,
    h:Int = 250,
    wmax:Option[Int] = None,
    hmax:Option[Int] = None,
    wmin:Option[Int] = None,
    hmin:Option[Int] = None,
    id:Int,
    pos:Option[Int] = Some(1),
    btype:Option[List[Int]] = None,
    battr:Option[List[Int]] = None,
    mimes:Option[List[String]] = Some(List("image/jpg", "image/png", "image/gif")),
    topframe:Option[Int] = Some(1),
    expdir:Option[List[Int]] = Some(List(1,2,3,4)),
    api:Option[List[Int]] = None
  )

  case class Video
  (mimes:Option[List[String]] = Some(List("video/mp4")),
   minduration:Option[Int] = Some(5),
   maxduration:Option[Int] = Some(30),
   protocol: Option[Int] = Some(3), // @TODO: WHat shoule the value be here?
   protocols:Option[List[Int]] = Some(List(3)),
   w:Int = 300,
   h:Int = 250,
   startdelay:Option[Int] = Some(0),
   linearity:Option[Int] = Some(1),
   sequence:Option[Int] = None,
   battr:Option[List[Int]] = None,
   maxextended:Option[Int] = None,
   minbitrate:Option[Int] = Some(500),
   maxbitrate:Option[Int] = Some(3000),
   boxingallowed:Option[Int] = Some(0),
   playbackmethod:Option[List[Int]] = None,
   delivery:Option[List[Int]] = Some(List(1, 2)),
   pos:Option[Int] = Some(0),
   companionad:List[Banner],
   api:Option[List[Int]] = Some(List(5)),
   companiontype:Option[List[Int]] = Some(List(1))
  )

  implicit  val json_Banner_2_2_1 = jsonFormat14(BidRequestModel_2_2_1.Banner)
  implicit  val json_Video_2_2_1 = jsonFormat21(BidRequestModel_2_2_1.Video)
  implicit  val json_Imp_2_2_1 = jsonFormat12(BidRequestModel_2_2_1.Imp)
  implicit  val json_bidRequest_2_2_1:RootJsonFormat[BidRequestModel_2_2_1.BidRequest] = jsonFormat14(BidRequestModel_2_2_1.BidRequest)
}

object BidRequestModel_2_4 extends DefaultJsonProtocol {

  import BidRequestCommon._

  case class BidRequest
  (id:String,
   imp:Imp,
   site:Option[Site] = None,
   app:Option[App] = None,
   device:Device,
   user:User,
   test:Option[Int] = Some(0),
   at:Option[Int] = Some(2),
   tmax:Option[Int] = Some(200), //@TODO: Set from application.conf
   wseat:Option[List[String]] = None,
   allimps:Option[Int] = Some(0),
   cur:Option[String] = None, //Some("USD"),
   bcat:Option[List[String]] = None, //@TODO: Need some values here
   badv:Option[List[String]] = None,
   bapp: Option[List[String]] = None,
   regs:Option[Regs] = None
  )

  case class Imp
  (id:String,
   banner:Option[Banner] = None,
   video:Option[Video] = None,
   audio: Option[Audio] = None,
   native:Option[Native] = None,
   displaymanager: Option[String] = None,
   displaymanagerver: Option[String] = None,
   instl:Option[Int] = Some(0),
   tagid: Option[String] = None,
   bidfloor:Option[Double] = Some(5.0),
   bidfloorcur: Option[String] = Some("USD"),
   clickbrowser: Option[Int] = None,
   secure:Option[Int] = Some(1), //@TODO: Need to change this accordingly
   iframebuster: Option[List[String]] = None,
   exp:Option[Int] = None,
   pmp:Option[Pmp] = None
  )

  case class Banner
  ( w:Int = 300,
    h:Int = 250,
    format: Option[Format] = None,
    id:Option[String] = None,
    pos:Option[Int] = Some(1),
    btype:Option[List[Int]] = None,
    battr:Option[List[Int]] = None,
    mimes:Option[List[String]] = Some(List("image/jpg", "image/png", "image/gif")),
    topframe:Option[Int] = Some(1),
    expdir:Option[List[Int]] = Some(List(1,2,3,4)),
    api:Option[List[Int]] = None
  )

  case class Format
  ( w:Int,
    h: Int
  )

  case class Video
  (mimes:Option[List[String]] = Some(List("video/mp4")),
   minduration:Option[Int] = Some(5),
   maxduration:Option[Int] = Some(30),
   protocols:Option[List[Int]] = Some(List(3)),
   w:Int = 300,
   h:Int = 250,
   startdelay:Option[Int] = Some(0),
   linearity:Option[Int] = Some(1),
   skip:Option[Int] = None,
   //    skipmin:Option[Int] = Some(0),
   //    skipafter:Option[Int] = Some(0),
   sequence:Option[Int] = None,
   battr:Option[List[Int]] = None,
   maxextended:Option[Int] = None,
   minbitrate:Option[Int] = Some(500),
   maxbitrate:Option[Int] = Some(3000),
   boxingallowed:Option[Int] = Some(0),
   playbackmethod:Option[List[Int]] = None,
   delivery:Option[List[Int]] = Some(List(1, 2)),
   pos:Option[Int] = Some(0),
   companionad:Option[List[Banner]] = Some(List(Banner())),
   api:Option[List[Int]] = Some(List(5)),
   companiontype:Option[List[Int]] = Some(List(1))
  )

  case class Audio
  ( mimes:Option[List[String]] = Some(List("audio/mp4")),
    minduration:Option[Int] = Some(5),
    maxduration:Option[Int] = Some(30),
    protocols:Option[List[Int]] = Some(List(3)),
    startdelay:Option[Int] = Some(0),
    sequence:Option[Int] = None,
    battr:Option[List[Int]] = None,
    maxextended:Option[Int] = None,
    minbitrate:Option[Int] = Some(500),
    maxbitrate:Option[Int] = Some(3000),
    delivery:Option[List[Int]] = Some(List(1, 2)),
    companionad:Option[List[Banner]] = Some(List(Banner())),
    api:Option[List[Int]] = Some(List(5)),
    companiontype:Option[List[Int]] = Some(List(1)),
    maxseq:Option[Int] = None,
    feed:Option[Int] = None,
    stitched:Option[Int] = None,
    nvol:Option[Int] = None
  )

  // Only version 2.3 onwards.
  case class Native
  ( request:String,
    ver:Option[String] = None,
    api:Option[List[Int]] = None,
    battr:Option[List[Int]] = None
  )

  implicit  val json_Format = jsonFormat2(BidRequestModel_2_4.Format)
  implicit  val json_Native = jsonFormat4(BidRequestModel_2_4.Native)
  implicit  val json_Banner_2_4 = jsonFormat11(BidRequestModel_2_4.Banner)
  implicit  val json_Audio_2_4 = jsonFormat18(BidRequestModel_2_4.Audio)
  implicit  val json_Video_2_4 = jsonFormat21(BidRequestModel_2_4.Video)
  implicit  val json_Imp_2_4 = jsonFormat16(BidRequestModel_2_4.Imp)
  implicit  val json_bidRequest_2_4:RootJsonFormat[BidRequestModel_2_4.BidRequest] = jsonFormat16(BidRequestModel_2_4.BidRequest)

}

object BidResponseModelCommon extends DefaultJsonProtocol {

  // TODO: Update Bid Response
  case class BidResponse
  ( id:String,
    seatbid:List[SeatBid],
    bidid:String,
    cur:Option[String] = Some("USD"),
    customdata:Option[String] = None,
    nbr:Option[Int] = None
  )

  case class SeatBid
  ( bid:List[Bid],
    seat:String,
    group:Option[Int]
  )

  case class Bid
  ( id:String,
    impid:String,
    price:Double,
    adid:String,
    nurl:Option[String],
    adm:Option[String],
    adomain:Option[List[String]],
    // ONly in 2.3.1 onwards
    bundle:Option[String],
    iurl:Option[String],
    cid:Option[String],
    crid:Option[String],
    // Only in 2.3.1 onwards
    cat:Option[List[String]],
    attr:Option[List[Int]],
    // Only in 2.3.1 onwards
    api:Option[Int],
    protocol:Option[Int],
    qagmediarating:Option[Int],
    dealid:Option[String],
    w:Int,
    h:Int,
    // Only in 2.3.1 onwards
    exp:Option[Int]
  )

  implicit val json_Bid = jsonFormat20(Bid)
  implicit val json_SeatBid = jsonFormat3(SeatBid)
  implicit val json_bidResponse:RootJsonFormat[BidResponse] = jsonFormat6(BidResponse)
}


