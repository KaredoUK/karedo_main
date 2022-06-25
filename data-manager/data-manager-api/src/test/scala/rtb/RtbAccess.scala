package rtb

import org.specs2.mutable._
import spray.json._
import spray.client.pipelining._

import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import spray.http._
import spray.client.pipelining._
import akka.actor.ActorSystem
import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class RtbAccess extends Specification with DefaultJsonProtocol {

  case class Location(countryCode: String, regionCode: String, cityName: String, dma: Int = -1, metro: Int = -1, timezoneOffsetMinutes: Int = -1)
  case class UserId(prov: Int, xchg: Int)

  case class Impression(id: Int, formats: List[String], position: Int = 0)
  case class Spot(id: Int, formats: List[String], position: Int = 0)
  case class Rtb(id: Long, timestamp: String, isTest: Boolean = false, url: String, language: String, exchange: String,
    location: Location, userIds: UserId, imp: List[Impression], spots: List[Spot])

  implicit val locationJson = jsonFormat6(Location)
  implicit val userIdJson = jsonFormat2(UserId)
  implicit val spotJson = jsonFormat3(Spot)
  implicit val impressionJson = jsonFormat3(Impression)
  implicit val rtbJson = jsonFormat10(Rtb)

  def str(): String = { // "id":"2508744128", 
    // "timestamp":"2016-08-10T21:27:29.450Z", "isTest":False, "url":"http://datacratic.com/", "language":"en", 
    // "exchange":"mock", 
    // "location":{"countryCode":"CA", "regionCode":"QC", "cityName":"Montreal", "dma":-1, "metro":-1, "timezoneOffsetMinutes":-1}, 
    // "userIds":{"prov":"1568485738", "xchg":"1475615520"}, 
    // "imp":[{"id":"1", "formats":["160x600"], "position":0}, {"id":"2", "formats":["160x600"], "position":0}], 
    //"spots":[{"id":"1", "formats":["160x600"], "position":0}, {"id":"2", "formats":["160x600"], "position":0}]
    val rtb =
      Rtb(id = 2508744128L, timestamp = "2016-08-10T21:27:29.450Z", url = "http://datacratic.com/", language = "en", exchange = "mock", location = Location(countryCode = "CA", regionCode = "QC", cityName = "Montreal"), userIds = UserId(prov = 1568485738, xchg = 1475615520), imp = List(Impression(id = 1, formats = List("160x600")), Impression(id = 2, formats = List("160x600"))), spots = List(Spot(id = 1, formats = List("160x600"))))
    val json = rtb.toJson.toString
    json
  }

  "Test" should {
    val apiLocation = "http://rtb.karedo.co.uk:12339"
    "compose json" in {

      val json = str()
      json must beEqualTo("""{"location":{"regionCode":"QC","cityName":"Montreal","metro":-1,"timezoneOffsetMinutes":-1,"dma":-1,"countryCode":"CA"},"timestamp":"2016-08-10T21:27:29.450Z","url":"http://datacratic.com/","exchange":"mock","id":2508744128,"language":"en","isTest":false,"userIds":{"prov":1568485738,"xchg":1475615520},"imp":[{"id":1,"formats":["160x600"],"position":0},{"id":2,"formats":["160x600"],"position":0}],"spots":[{"id":1,"formats":["160x600"],"position":0}]}""")
    }

    "we can call rtb" in {
      implicit val system = ActorSystem()
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

      val result = pipeline(Post(s"$apiLocation/bids", str()))
      //Thread.sleep(10000)
      result onComplete { x => 
        ??? 
        }
      
        
      "" must beEqualTo("")  
      Thread.sleep(10000)
    }
  }

}