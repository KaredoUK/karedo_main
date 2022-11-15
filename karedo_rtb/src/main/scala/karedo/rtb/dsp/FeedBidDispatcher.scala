package karedo.rtb.dsp

import karedo.entity.UserPrefData
import karedo.rtb.model.AdModel._

import scala.concurrent.{ExecutionContext, Future}
import karedo.rtb.model.BidRequestCommon._
import karedo.rtb.util.{DeviceMake, LoggingSupport, RtbConstants}

import karedo.rtb.model.BidJsonImplicits

import scala.xml._
import java.net.URL
import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util.{Failure, Success, Try}

import collection.JavaConverters._
import scala.collection.JavaConverters._

import org.jsoup._

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

    val fSeq = Future.sequence(
      urls.map(url => Future(
        rssReader.read(url)
      ))
    )

    val feeds: Seq[Seq[RssFeed]] = Await.result(
      fSeq.mapTo[Seq[Seq[RssFeed]]], dispatcher_max_wait
    )


//    java.util.Base64.getDecoder.decode("").toString

    val ads:Seq[Seq[AdUnit]] = feeds.flatten.map(feed => feed.items.map(item => {
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

      AdUnit(
        ad_type = ad_type_NATIVE,
        ad_id = item.guid,
        impid = item.guid,
        ad = Ad(
          imp_url = enclosure match {
            case Some(url) => url.url
            case None => feed.image_url
//              getLargestImageUrl(item.link) match {
//              case Some(u) => u
//              case None => feed.image_url
//            }
          },
          click_url = item.link,
          ad_text = item.title,
          ad_source = Some(feed.source),
          duration = None,
          h = Some(250),
          w = Some(300),
          beacons = None
        ),
        price = if(ad_type == ad_type_NATIVE) 1.0 else if(ad_type == ad_type_VIDEO) 0.9 else 0.95,
        ad_domain = Some(List(ad_domain)),
        iurl = Some(item.link),
        nurl = Some(item.link),
        cid = item.guid,
        crid = item.guid,
        w = 300,
        h = 250,
        hint = Math.random()
      )
    }))

    ads.flatten.sortWith((a,b) => a.hint > b.hint).take(count).toList
  }

  def getUrls = {
   val confList = config.config.getConfigList("feeds")
   confList.asScala.map(c => {
     RssUrl(
       name = c.getString("name"),
       feed_url = new URL(c.getString("feed_url")),
       image_url = c.getString("img_url")
     )
   })
  }


}

abstract class Reader extends RtbConstants  {
  def extract(xml:Elem, name:String, image_url:String):Seq[RssFeed]

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

  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH);

  private def parseAtomDate(date:String, formatter:SimpleDateFormat):Date = {
    val newDate = date.reverse.replaceFirst(":", "").reverse
    return formatter.parse(newDate)
  }

  private def getHtmlLink(node:NodeSeq) = {
    node
      .filter(n => (n \ "@type").text == "text/html")
      .map( n => (n \ "@href").text).head
  }

  override def extract(xml:Elem, name:String, image_url:String) : Seq[RssFeed] = {
    for (feed <- xml \\ "feed") yield {
      val items = for (item <- (feed \\ "entry")) yield {
        RssItem(
          title = (item \\ "title").text.replaceAll("\n", "").replaceAll("\t", ""),
          link = getHtmlLink((item \\ "link")).replaceAll("\n", "").replaceAll("\t", ""),
          desc = (item \\ "summary").text,
//          date = parseAtomDate((item \\ "published").text, dateFormatter),
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
        image_url = image_url
      )
    }
  }
}

class XmlReader extends Reader {

  val dateFormatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

  override def extract(xml:Elem, name:String, image_url:String) : Seq[RssFeed] = {

    for (channel <- xml \\ "channel") yield {
      val items = for (item <- (channel \\ "item")) yield {
        RssItem(
          title = (item \\ "title").text.replaceAll("\n", "").replaceAll("\t", ""),
          link = (item \\ "link").text.replaceAll("\n", "").replaceAll("\t", ""),
          desc = (item \\ "description").text,
//          date = dateFormatter.parse((item \\ "pubDate").text),
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
        image_url = image_url
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

        actor.extract(xml, url.name, url.image_url)
      }
      case Failure(_) => Seq()
    }
  }
}


case class RssUrl(name:String, feed_url:URL, image_url:String) {
  override def toString = s"RSS: ${name} -> ${feed_url.toString}"
}

trait RssFeed {
  val source:String
  val link:String
  val title:String
  val desc:String
  val image_url:String
  val items:Seq[RssItem]
  override def toString = title + "\n" + desc + "\n**"

//  def latest = sorted head
  def sorted = items sortWith ((a, b) => a.randonHint > b.randonHint)
}

case class AtomRssFeed(title:String, link:String, desc:String, items:Seq[RssItem], source:String, image_url:String) extends RssFeed
case class XmlRssFeed(title:String, link:String, desc:String, language:String, items:Seq[RssItem], source:String, image_url:String) extends RssFeed

case class RssItem(title:String,
                   link:String,
                   desc:String,
//                   date:Date,
                   guid:String,
                   enclosure:Seq[RssEnclosure] = Nil,
                   randonHint: Double )

case class RssEnclosure(url:String, length:Int, mime: String)
