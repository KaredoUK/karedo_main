package karedo.rtb.model


/**
  * Created by crajah on 11/08/2016.
  *
  * Contains the case classes for OpenRTB version 2.3.1
  */

object BidModel {
  import spray.json._

  import DefaultJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  case class BidRequest_2_4(id:String,
                              imp:Imp_2_4,
                              site:Option[Site] = None,
                              app:Option[App] = None,
                              device:Device_2_4,
                              user:User,
                              test:Option[Int] = Some(0),
                              at:Option[Int] = Some(2),
                              tmax:Option[Int] = Some(200),
                              wseat:Option[List[String]] = None,
                              allimp:Option[Int] = Some(0),
                              cur:Option[String] = Some("USD"),
                              bcat:Option[String] = None,
                              badv:Option[List[String]] = None,
                              regs:Option[Regs] = None)

  case class BidRequest_2_2_1(id:String,
                              imp:Imp_2_2_1,
                              site:Option[Site] = None,
                              app:Option[App] = None,
                              device:Device_2_2_1,
                              user:User,
                              at:Option[Int] = Some(2),
                              tmax:Option[Int] = Some(200),
                              wseat:Option[List[String]] = None,
                              allimp:Option[Int] = Some(0),
                              cur:Option[String] = Some("USD"),
                              bcat:Option[String] = None,
                              badv:Option[List[String]] = None,
                              regs:Option[Regs] = None)

  case class Imp_2_2_1(id:String,
                       banner:Option[Banner_2_2_1] = None,
                       video:Option[Video] = None,
                       displaymanager:Option[String] = None,
                       displaymanagerver:Option[String] = None,
                       instl:Option[Int] = None,
                       tagid:Option[String] = None,
                       bidfloor:Option[Double] = Some(5.0),
                       bidfloorcur:Option[String] = None,
                       secure:Option[Int] = Some(0),
                       iframebuster:Option[List[String]] = None,
                       pmp:Option[Pmp] = None)

  case class Imp_2_4(id:String,
                       banner:Option[Banner_2_4] = None,
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
                       secure:Option[Int] = Some(0),
                     iframebuster: Option[List[String]] = None,
                     exp:Option[Int] = None,
                       pmp:Option[Pmp] = None)

  case class Banner_2_4(w:Int = 300,
                    h:Int = 250,
                        format: Option[Format] = None,
//                    wmax:Option[Int] = None,
//                    hmax:Option[Int] = None,
//                    wmin:Option[Int] = None,
//                    hmin:Option[Int] = None,
                    id:Option[String] = None,
                    pos:Option[Int] = Some(0),
                    btype:Option[List[Int]] = None,
                    battr:Option[List[Int]] = None,
                    mimes:Option[List[String]] = Some(List("image/jpg", "image/png", "image/gif")),
                    topframe:Option[Int] = Some(1),
                    expdir:Option[List[Int]] = Some(List(1,2,3,4)),
                    api:Option[List[Int]] = None)

  case class Banner_2_2_1(w:Int = 300,
                    h:Int = 250,
                    wmax:Option[Int] = None,
                    hmax:Option[Int] = None,
                    wmin:Option[Int] = None,
                    hmin:Option[Int] = None,
                    id:Option[String] = None,
                    pos:Option[Int] = Some(0),
                    btype:Option[List[Int]] = None,
                    battr:Option[List[Int]] = None,
                    mimes:Option[List[String]] = Some(List("image/jpg", "image/png", "image/gif")),
                    topframe:Option[Int] = Some(1),
                    expdir:Option[List[Int]] = Some(List(1,2,3,4)),
                    api:Option[List[Int]] = None)

  case class Format(w:Int, h: Int)

  case class Video_2_4(mimes:Option[List[String]] = Some(List("video/mp4")),
                   minduration:Option[Int] = Some(5),
                   maxduration:Option[Int] = Some(30),
                   protocols:Option[List[Int]] = Some(List(3)),
                   w:Int = 300,
                   h:Int = 250,
                   startdelay:Option[Int] = Some(0),
                   linearity:Option[Int] = Some(1),
                       skip:Option[Int] = None,
                       skipmin:Option[Int] = Some(0),
                       skipafter:Option[Int] = Some(0),
                   sequence:Option[Int] = None,
                   battr:Option[List[Int]] = None,
                   maxextended:Option[Int] = None,
                   minbitrate:Option[Int] = Some(500),
                   maxbitrate:Option[Int] = Some(3000),
                   boxingallowed:Option[Int] = Some(0),
                   playbackmethod:Option[List[Int]] = None,
                   delivery:Option[List[Int]] = Some(List(1, 2)),
                   pos:Option[Int] = Some(0),
                   companionad:Option[List[Banner_2_4]] = Some(List(Banner_2_4())),
                   api:Option[List[Int]] = Some(List(5)),
                   companiontype:Option[List[Int]] = Some(List(1)) )

  case class Video_2_2_2(mimes:Option[List[String]] = Some(List("video/mp4")),
                   minduration:Option[Int] = Some(5),
                   maxduration:Option[Int] = Some(30),
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
                   companionad:Option[List[Banner_2_2_1]] = Some(List(Banner_2_2_1())),
                   api:Option[List[Int]] = Some(List(5)),
                   companiontype:Option[List[Int]] = Some(List(1)) )

  case class Audio_2_4
  (
    mimes:Option[List[String]] = Some(List("audio/mp4")),
      minduration:Option[Int] = Some(5),
  maxduration:Option[Int] = Some(30),
  protocols:Option[List[Int]] = Some(List(3)),
  )

  // Only version 2.3 onwards.
  case class Native(request:String,
                    ver:Option[String] = None,
                    api:Option[List[Int]] = None,
                    battr:Option[List[Int]] = None)

  // Site or App => Not both
  case class Site(id:String,
                  name:Option[String] = Some("karedo"),
                  domain:Option[String] = Some("karedo.co.uk"),
                  cat:Option[List[String]] = None,
                  sectioncat:Option[List[String]] = None,
                  pagecat:Option[List[String]] = None,
                  page:Option[String] = Some("http://www.karedo.co.uk/main"),
                  ref:Option[String] = None,
                  search:Option[String] = None,
                  // Used only 2.3.1 and optional
                  mobile:Option[Int] = None,
                  privacypolicy:Option[Int] = Some(0),
                  publisher:Option[Publisher] = None,
                  content:Option[Content] = None,
                  keywords:Option[String] = None)

  // Site or App => Not both
  case class App(id:String,
                 name:Option[String] = Some("karedo"),
                 domain:Option[String] = Some("karedo.co.uk"),
                 storeurl:Option[String] = None,
                 cat:Option[List[String]] = None,
                 sectioncat:Option[List[String]] = None,
                 pagecat:Option[List[String]] = None,
                 ver:Option[String] = Some("1.0"),
                 bundle:Option[String] = Some("karedo"),
                 privacypolicy:Option[Int] = Some(0),
                 paid:Option[Int] = Some(0),
                 publisher:Option[Publisher] = None,
                 content:Option[Content] = None,
                 keywords:Option[String] = None)

  case class Publisher(id:String,
                       name:Option[String] = Some("karedo"),
                       cat:Option[List[String]] = None,
                       domain:Option[String] = Some("karedo.co.uk"))

  case class Content(id:String,
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
                     embeddable:Option[Int] = None)

  case class Producer(id:String,
                      name:Option[String] = Some("karedo"),
                      cat:Option[List[String]] = None,
                      domain:Option[String] = Some("karedo.co.uk"))

  case class Device_2_2_1(
                           dnt:Option[Int] = Some(1),
                           ua:Option[String] = None,
                           ip:Option[String] = Some("127.0.0.1"),
                          geo:Option[Geo] = None,
                           didsha1:Option[String] = None,
                           didmd5:Option[String] = None,
                           dpidsha1:Option[String] = None,
                           dpidmd5:Option[String] = None,
                           macsha1:Option[String] = None,
                           macmd5:Option[String] = None,
                           ipv6:Option[String] = None,
                           carrier:Option[String] = None,
                           language:Option[String] = None,
                           make:Option[String] = None,
                           model:Option[String] = None,
                           os:Option[String] = None,
                           osv:Option[String] = None,
                           js:Option[Int] = Some(0),
                           connectiontype:Option[Int] = None,
                           devicetype:Option[Int] = Some(1),
                           flashver:Option[String] = None,
                           ifa:Option[String] = None
                         )

  case class Device_2_3_1(ua:Option[String] = None,
                    geo:Option[Geo] = None,
                    dnt:Option[Int] = Some(1),
                    lmt:Option[Int] = Some(1),
                    ip:Option[String] = Some("127.0.0.1"),
//                    ipv6:Option[String] = None,
                    devicetype:Option[Int] = Some(1),
                    make:Option[String] = None,
                    model:Option[String] = None,
                    os:Option[String] = None,
                    osv:Option[String] = None,
                    // Used on in 2.3.1 onwards
                    hwv:Option[String] = None,
                    h:Option[Int] = Some(600),
                          w:Option[Int] = Some(300),
                          ppi:Option[Int] = Some(200),
                    pxratio:Option[Double] = Some(1.17),
                    js:Option[Int] = Some(0),
//                    flashver:Option[String] = None,
                    language:Option[String] = None,
//                    carrier:Option[String] = None, connectiontype:Option[Int] = None,
                    ifa:Option[String] = None,
//                   IMEI - Not included.
//                    didsha1:Option[String] = None, didmd5:Option[String] = None,
                    pdidsha1:Option[String] = None,
                          dpidmd5:Option[String] = None,
                    macsha1:Option[String] = None,
                          macmd5:Option[String] = None)


  case class Geo(lat:Double,
                 lon:Double,
                 $type:Int = 1,
                 country:String = "GB",
                 region:Option[String] = None,
                 regionfips104:Option[String] = None,
                 metro:Option[String] = None,
                 city:Option[String] = None,
                 zip:Option[String] = None,
                 // Only in 2.3.1 onwards
                 utoffset:Option[Int] = None
                )

  case class User(id:String,
                  buyeruid:String,
                  yob:Option[Int] = None,
                  gender:Option[String] = None,
                  keywords:Option[String] = None,
                  customdata:Option[String] = None,
                  geo:Option[Geo] = None,
                  data:Option[List[Data]] = None)

  case class Data(id:Option[String] = None, name:Option[String] = None,
                  segment:Option[List[Segment]] = None)

  case class Segment(id:Option[String] = None, name:Option[String] = None, value:Option[String] = None)

  case class Regs(coppa:Option[Int] = None)

  case class Pmp(private_auction:Option[Int] = None, deals:Option[List[Deal]] = None)

  case class Deal(id:String, bidfloor:Option[Double] = Some(0), bidfloorcur:Option[String] = Some("USD"),
                  at:Option[Int] = None, wseat:Option[List[String]] = None,
                  wadomain:Option[List[String]] = None)


// TODO: Update Bid Response
  case class BidResponse(id:String,
                               seatbid:List[SeatBid],
                               bidid:String,
                               cur:Option[String] = Some("USD"),
                               customdata:Option[String],
                               nbr:Option[Int]
                              )

  case class SeatBid(bid:List[Bid],
                     seat:String,
                     group:Option[Int])

  case class Bid(id:String,
                 impid:String,
                 price:Double,
                 adid:String,
                 nurl:String,
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
                 exp:Option[Int])



  implicit val deal = jsonFormat6(Deal)
  implicit val pmp = jsonFormat2(Pmp)
  implicit val regs = jsonFormat1(Regs)
  implicit val segment = jsonFormat3(Segment)
  implicit val data = jsonFormat3(Data)
  implicit val geo = jsonFormat10(Geo)
  implicit val user = jsonFormat8(User)
  implicit val device_2_3_1 = jsonFormat22(Device_2_3_1)
  implicit val device_2_2_1 = jsonFormat22(Device_2_2_1)
  implicit val producer = jsonFormat4(Producer)
  implicit val content = jsonFormat19(Content)
  implicit val publisher = jsonFormat4(Publisher)
  implicit val app = jsonFormat14(App)
  implicit val site = jsonFormat14(Site)
  implicit val native = jsonFormat4(Native)
  implicit val banner = jsonFormat14(Banner)
  implicit val video = jsonFormat20(Video)
  implicit val imp_2_3_1 = jsonFormat8(Imp_2_3_1)
  implicit val imp_2_2_1 = jsonFormat12(Imp_2_2_1)


  implicit val bid = jsonFormat20(Bid)
  implicit val seatBid = jsonFormat3(SeatBid)

  implicit val bidRequest_2_3_1:RootJsonFormat[BidRequest_2_3_1] = jsonFormat15(BidRequest_2_3_1)
  implicit val bidRequest_2_2_1:RootJsonFormat[BidRequest_2_2_1] = jsonFormat14(BidRequest_2_2_1)

  implicit val bidResponse:RootJsonFormat[BidResponse] = jsonFormat6(BidResponse)
}