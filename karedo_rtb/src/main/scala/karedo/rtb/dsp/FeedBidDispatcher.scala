package karedo.rtb.dsp

import karedo.entity.{AdType, AdUnitType, Feed, UserPrefData}
import karedo.rtb.model.AdModel._

import scala.concurrent.{ExecutionContext, Future}
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.{DeviceMake, LoggingSupport, RtbConstants}
import karedo.rtb.model.BidJsonImplicits

import scala.xml._
import java.net.URL
import java.text.SimpleDateFormat
import java.util.concurrent.Executors
import java.util.{Base64, Date, Locale}

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{ContentTypes, HttpRequest, Uri}
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal
import karedo.rtb.dsp.AdMechanic.httpDispatcher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success, Try}
import collection.JavaConverters._
import scala.collection.JavaConverters._
import org.jsoup._

import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import com.google.common.net.InternetDomainName
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}
import org.slf4j.LoggerFactory


object FeedLoader extends RtbConstants with LoggingSupport with DefaultJsonProtocol with HttpDispatcher {
  private val rssReader = new RssReader

  def getAdsFromFeeds(origFeeds: List[Feed], native_cpm: Double, adPostfix: Option[AdUnitType => AdUnitType] = None): List[AdUnitType] = {
    logger.debug("Feeds to extract: " + origFeeds)

    val enabledFeeds = origFeeds.filter(_.enabled)

    logger.debug("Only Enabled Feeds: " + enabledFeeds)

    getAdsFromRssUrl(enabledFeeds.map(feedToRssUrl(_)), native_cpm, adPostfix)
  }

  def getAdsFromRssUrl(urls: List[RssUrl], native_cpm: Double, adPostfix: Option[AdUnitType => AdUnitType] = None): List[AdUnitType] = {
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

    val fSeq = Future.sequence(
      urls.map(url => Future(
        rssReader.read(url)
      ))
    )

    val rssfeeds: Seq[Seq[RssFeed]] = Await.result(
      fSeq.mapTo[Seq[Seq[RssFeed]]], 10 minutes // @TODO: Should this be infinite?
    )

    val ads:Seq[Seq[AdUnitType]] = rssfeeds.flatten.map(feed => feed.items.map(item => {
      val images = item.enclosure.filter(_.mime.startsWith("image"))
      val videos = item.enclosure.filter(_.mime.startsWith("video"))

      var ad_type = ad_type_NATIVE

      val enclosure: Option[RssEnclosure] = if(! images.isEmpty) {
        ad_type = ad_type_NATIVE
        Some(images.head)
      } else if (! videos.isEmpty) {
        ad_type = ad_type_VIDEO
        Some(videos.head)
      } else {
        ad_type = ad_type_NATIVE
        None
      }

      var ad_domain = ""

      try {
        ad_domain = (new URL(item.link)).getHost
      } catch {
        case e:Exception => logger.error(s"${feed.source} => ${item.title} => ${item.link} => getHostFailed", e)
      }

      val article_elements:(Option[String], Option[String], Option[String], Option[String]) = getItemsFromUrl(item.link)

      val ad = AdUnitType(
        ad_type = ad_type_NATIVE,
        id = Base64.getEncoder.encodeToString(java.security.MessageDigest.getInstance("MD5").digest(s"${feed.source}::${item.guid}".getBytes)) ,
        impid = item.guid,
        ad = AdType(
          imp_url = article_elements._1 match {
            case Some(s) => s
            case None => enclosure match {
              case Some(s) => s.url
              case None => feed.image_url
            }
          },
          click_url = item.link,
          ad_text = article_elements._2.getOrElse(item.title),
          ad_source = article_elements._3 match {case Some(x) => Some(x) case None => Some(feed.source)} ,
          duration = None,
          h = Some(250),
          w = Some(300),
          beacons = None
        ),
        price_USD_per_1k =
          (if(ad_type == ad_type_NATIVE) native_cpm
          else if(ad_type == ad_type_VIDEO) (native_cpm - 0.1)
          else (native_cpm - 0.5)),
        ad_domain = Some(List(ad_domain)),
        iurl = Some(item.link),
        nurl = Some(item.link),
        cid = item.guid,
        crid = item.guid,
        w = 300,
        h = 250,
        hint = Math.random(),
        prefs = feed.prefs,
        source = feed.source,
        locale = article_elements._4,
        pub_date = item.pub_date
      )

      adPostfix match {case Some(f) => f(ad) case None => ad}
    }))

    ads.flatten.toList
  }

  private def feedToRssUrl(feed: Feed): RssUrl = {
    RssUrl(
      name = feed.name,
      feed_url = new URL(feed.url),
      image_url = feed.fallback_img,
      prefs = feed.prefs
    )
  }

  private def getBestImage(click_url: String): Option[String] = {
    case class ArticleExtract
    (
      article: String
      , author: String
      , feeds: List[String]
      , image: Option[String]
      , keywords: List[String]
      , publishDate: String
      , title: String
      , videos: List[String]
    )

    implicit val json_ArticleExtract: RootJsonFormat[ArticleExtract] = jsonFormat8(ArticleExtract)

    val params = Map("best_image" -> "true", "url" -> click_url)

    val uri = Uri("https://api.aylien.com/api/v1/extract").withQuery(Query(params))

    // "X-AYLIEN-TextAPI-Application-Key: YOUR_APP_KEY" \
    // -H "X-AYLIEN-TextAPI-Application-ID: YOUR_APP_ID

    val headers = List(RawHeader("X-AYLIEN-TextAPI-Application-Key", "912d430f"),
      RawHeader("X-AYLIEN-TextAPI-Application-ID", "c5c644c5445fa76ec034058db788d1a3"))

    val f:Future[ArticleExtract] = httpsDispatcher.singleRequest(HttpRequest(
      GET,
      uri = uri,
      headers = headers
    )
    ).flatMap { response =>
      logger.info(response.toString)
      Unmarshal(response.entity.withContentType(ContentTypes.`application/json`)).to[ArticleExtract]
    }

    val articleExtract: ArticleExtract = Await.result(f, 5 minutes)

    articleExtract.image
  }

  private def getImageSize(url: String): Long = {
    try {
      Await.result(
        httpsDispatcher.singleRequest(HttpRequest(GET, Uri(url))).map(r => r.entity.getContentLengthOption().orElse(0)),
        20 seconds)
    } catch {
      case e:Exception => {
        logger.error("unable to get images", e)
        0:Long
      }
    }
  }

  private def getItemsFromUrl(url: String): (Option[String], Option[String], Option[String], Option[String]) = {
    try {
      val doc = Jsoup.connect(url).get()
      val metas = doc.select("meta").asScala

//      println("Metas: " + metas )

      val article_image = metas
        .filter(_.attr("property").equalsIgnoreCase("og:image"))
        .map(_.attr("content").toString).toList match {
        case Nil => None
        case l => Some(l.head)
      }

//      val img_meta = metas.filter(_.attr("property").equalsIgnoreCase("og:image"))
//
//      val img_urls = img_meta.map(_.attr("content").toString)
//
//      val image_url = if(img_urls.isEmpty) None
//      else Some(img_urls.head)

      val article_titile = metas
        .filter(_.attr("property").equalsIgnoreCase("og:title"))
        .map(_.attr("content").toString).toList match {
        case Nil => None
        case l => Some(l.head)
      }

      val article_source = metas
        .filter(_.attr("property").equalsIgnoreCase("og:site_name"))
        .map(_.attr("content").toString).toList match {
        case Nil => None
        case l => Some(l.head)
      }

      val article_locale = metas
        .filter(_.attr("property").equalsIgnoreCase("og:locale"))
        .map(_.attr("content").toString).toList match {
        case Nil => None
        case l => Some(l.head)
      }

      (article_image, article_titile, article_source, article_locale)


      //      val host = new URL(url).getHost
      //      val privateDomain:String = InternetDomainName.from(host).topPrivateDomain.toString
      //
      //      println("Host: " + host + " Pvt DOmain: " + privateDomain)


      //      val images = doc.select("img")
//
//      val imageSrcs = images.asScala.map(_.attr("src"))
//
//      val img = images.asScala
//        .filter(i => { ! (i.attr("height").isEmpty || i.attr("width").isEmpty) })
//        .map(_.attr("src"))
//        .filter(_.contains(privateDomain))

//        .map { i =>
//          val size: Long = getImageSize(i)
//          (i, size)
//        }
//        .maxBy(i => i._2)
//        ._1

//        Some(img.head)

        //     val imgColl = images.asScala
        //       .filter(i => { ! (i.attr("height").isEmpty || i.attr("width").isEmpty) })
        //       .map(i => {
        //         val url = i.attr("src")
        //
        //         var ha = i.attr("height")
        //         var wa = i.attr("width")
        //
        //         ha = if(ha.endsWith("px")) ha.substring(0, ha.indexOf("px")) else ha
        //         wa = if(wa.endsWith("px")) wa.substring(0, wa.indexOf("px")) else wa
        //
        //         ha = if(ha.endsWith("%")) ha.substring(0, ha.indexOf("%")) else ha
        //         wa = if(wa.endsWith("%")) wa.substring(0, wa.indexOf("%")) else wa
        //
        //         val h = ha.toInt
        //         val w = wa.toInt
        //
        //         val size = if((h > 5 && w > 5)) h * w else 0
        //
        //         (url, size)
        //       })
        //
        //     val oUrl = if(imgColl.isEmpty) images.first().attr("src") else imgColl.maxBy(_._2)._1

//        Some(images.first().attr("src"))

    } catch {
      case e:Exception => {
        (None, None, None, None)
      }
    }
  }

}

/**
  * Created by charaj on 27/12/2016.
  */
class FeedBidDispatcher(config: DspBidDispatcherConfig)
  extends DspBidDispather(config)
    with LoggingSupport
    with BidJsonImplicits
    with RtbConstants
     {

  lazy val class_name = this.getClass.getName

  override def getAds(count: Int, user: User, device: Device, iabCatMap: Map[String, UserPrefData], make: DeviceMake, deviceRequest: DeviceRequest): List[AdUnit] = {
    logger.debug(s"IN: ${class_name}.getAds. Count is ${count}, User is: ${user}, Device is ${device}" )

    val adUnits = scala.collection.mutable.ListBuffer.empty[AdUnit]

    val rssReader = new RssReader

    val urls = getUrls

    val adUnitTypes = FeedLoader.getAdsFromRssUrl(urls.toList, config.price_cpm)

    val ads = AdMechanic.adUntiTypeToAdUnit(adUnitTypes)

    ads.sortWith((a,b) => a.hint > b.hint).take(count)
  }

  def getUrls = {
   val confList = config.config.getConfigList("feeds")
   confList.asScala.map(c => {
     RssUrl(
       name = c.getString("name"),
       feed_url = new URL(c.getString("feed_url")),
       image_url = c.getString("img_url"),
       prefs = List()
     )
   })
  }


}

abstract class Reader extends RtbConstants  {
  def extract(xml:Elem, name:String, image_url:String, prefs:List[String]):Seq[RssFeed]

  val logger = LoggerFactory.getLogger(classOf[Reader])

  def print(feed:RssFeed) {
    println(feed.items)
  }

  def getLargestImageUrl(url: String): Option[String] = {
    try {
      Await.result(Future {
        val doc = Jsoup.connect(url).get()
        val images = doc.select("img")

        //     val imageSrcs = images.asScala.map(_.attr("src"))
        //
        //     val imgColl = images.asScala
        //       .filter(i => { ! (i.attr("height").isEmpty || i.attr("width").isEmpty) })
        //       .map(i => {
        //         val url = i.attr("src")
        //
        //         var ha = i.attr("height")
        //         var wa = i.attr("width")
        //
        //         ha = if(ha.endsWith("px")) ha.substring(0, ha.indexOf("px")) else ha
        //         wa = if(wa.endsWith("px")) wa.substring(0, wa.indexOf("px")) else wa
        //
        //         ha = if(ha.endsWith("%")) ha.substring(0, ha.indexOf("%")) else ha
        //         wa = if(wa.endsWith("%")) wa.substring(0, wa.indexOf("%")) else wa
        //
        //         val h = ha.toInt
        //         val w = wa.toInt
        //
        //         val size = if((h > 5 && w > 5)) h * w else 0
        //
        //         (url, size)
        //       })
        //
        //     val oUrl = if(imgColl.isEmpty) images.first().attr("src") else imgColl.maxBy(_._2)._1

        Some(images.first().attr("src"))

      }, rtb_max_wait)
    } catch {
      case e:Exception => {
        None
      }
    }
  }
}

class AtomReader extends Reader {

  val dateFormatter = ISODateTimeFormat.dateTime()

  private def parseAtomDate(date:String):DateTime = {
    try {
      dateFormatter.parseDateTime(date)
    } catch {
      case e: Exception => logger.error("Date Parsing Failed", e)
        karedo.util.Util.now
    }
  }

  private def getHtmlLink(node:NodeSeq) = {
    node
      .filter(n => (n \ "@type").text == "text/html")
      .map( n => (n \ "@href").text).head
  }

  override def extract(xml:Elem, name:String, image_url:String, prefs:List[String]) : Seq[RssFeed] = {
    for (feed <- xml \\ "feed") yield {
      val items = for (item <- (feed \\ "entry")) yield {
        RssItem(
          title = (item \\ "title").text.replaceAll("\n", "").replaceAll("\t", ""),
          link = getHtmlLink((item \\ "link")).replaceAll("\n", "").replaceAll("\t", ""),
          desc = (item \\ "summary").text,
          pub_date = parseAtomDate((item \\ "published").text),
          guid = (item \\ "id").text.replaceAll("\n", "").replaceAll("\t", ""),
          enclosure = (item \\ "link")
            .filter(n => (n \ "@type").text.startsWith("image") || (n \ "@type").text.startsWith("video"))
            .map(n => {
              val enLen = (n \\ "@length").text
              val enLenInt:Int = if(enLen != null && ! enLen.isEmpty) enLen.toInt else 0

              RssEnclosure(
              url = (n \ "@href").text.replaceAll("\n", "").replaceAll("\t", ""),
              length = enLenInt,
              mime = (n \ "@type").text
            )}),
          randonHint = Math.random()
        )
      }
      AtomRssFeed(
        title = (feed \ "title").text.replaceAll("\n", "").replaceAll("\t", ""),
        link = getHtmlLink((feed \ "link")).replaceAll("\n", "").replaceAll("\t", ""),
        desc = (feed \ "subtitle ").text,
        items = items,
        source = name,
        image_url = image_url,
        prefs = prefs
      )
    }
  }
}

class XmlReader extends Reader {

  val dateFormatZZZ = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss ZZZ").withLocale(Locale.ENGLISH).withOffsetParsed()
  val dateFormatZ = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").withLocale(Locale.ENGLISH).withOffsetParsed()

  private def parseRssDate(date:String):DateTime = {
    try {
      date match {
        case s if s.contains("+") || s.contains("_") => dateFormatZ.parseDateTime(s)
        case s => dateFormatZZZ.parseDateTime(s)
      }
    } catch {
      case e: Exception => logger.error("Date Parsing Failed", e)
        karedo.util.Util.now
    }
  }

  override def extract(xml:Elem, name:String, image_url:String, prefs:List[String]) : Seq[RssFeed] = {

    for (channel <- xml \\ "channel") yield {
      val items = for (item <- (channel \\ "item")) yield {
        RssItem(
          title = (item \\ "title").text.replaceAll("\n", "").replaceAll("\t", ""),
          link = (item \\ "link").text.replaceAll("\n", "").replaceAll("\t", ""),
          desc = (item \\ "description").text,
          pub_date = parseRssDate((item \\ "pubDate").text),
          guid = (item \\ "guid").text.replaceAll("\n", "").replaceAll("\t", ""),
          enclosure = (item \\ "enclosure").filter(n => (n \ "@type").text.startsWith("image") || (n \ "@type").text.startsWith("video"))
            .map(
            n => {
              val enLen = (n \\ "@length").text
              val enLenInt:Int = if(enLen != null && ! enLen.isEmpty) enLen.toInt else 0

              RssEnclosure(
              url = (n \\ "@url").text.replaceAll("\n", "").replaceAll("\t", ""),
              length =  enLenInt,
              mime = (n \\ "@type").text
            )}
          )
//          match {
//            case h::t => h::t
//            case Nil => getLargestImageUrl((item \\ "link").text) match {
//              case Some(u) => Seq(RssEnclosure(u, 0, "image/jpg"))
//              case None => Nil
//            }
//          }
          ,
          randonHint = Math.random()
        )
      }
      XmlRssFeed(
        title = (channel \ "title").text.replaceAll("\n", "").replaceAll("\t", ""),
        link = (channel \ "link").text.replaceAll("\n", "").replaceAll("\t", ""),
        desc = (channel \ "description").text,
        language = (channel \ "language").text,
        items = items,
        source = name,
        image_url = image_url,
        prefs = prefs
      )
    }
  }
}


class RssReader {
  val xmlReader = new XmlReader
  val atomReader = new AtomReader

  def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try { op(p) } finally { p.close() }
  }

  def read(url : RssUrl):Seq[RssFeed] = {
    Try(url.feed_url.openConnection.getInputStream) match {
      case Success(u) => {
        val xml = XML.load(u)
        val actor = if((xml \\ "channel").length == 0) atomReader else xmlReader

        actor.extract(xml, url.name, url.image_url, url.prefs)
      }
      case Failure(_) => Seq()
    }
  }
}


case class RssUrl(name:String, feed_url:URL, image_url:String, prefs: List[String]) {
  override def toString = s"RSS: ${name} -> ${feed_url.toString}"
}

trait RssFeed {
  val source:String
  val link:String
  val title:String
  val desc:String
  val image_url:String
  val items:Seq[RssItem]
  val prefs:List[String]
  override def toString = title + "\n" + desc + "\n**"

//  def latest = sorted head
  def sorted = items sortWith ((a, b) => a.randonHint > b.randonHint)
}

case class AtomRssFeed(title:String, link:String, desc:String, items:Seq[RssItem], source:String, image_url:String,prefs:List[String]) extends RssFeed
case class XmlRssFeed(title:String, link:String, desc:String, language:String, items:Seq[RssItem], source:String, image_url:String,prefs:List[String]) extends RssFeed

case class RssItem(title:String,
                   link:String,
                   desc:String,
                   pub_date:DateTime,
                   guid:String,
                   enclosure:Seq[RssEnclosure] = Nil,
                   randonHint: Double )

case class RssEnclosure(url:String, length:Int, mime: String)
