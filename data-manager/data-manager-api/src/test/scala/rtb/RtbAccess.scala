package rtb

import java.util.concurrent.TimeUnit

import org.specs2.mutable._
import spray.json._
import org.specs2.runner.JUnitRunner
import org.junit.runner.RunWith
import com.parallelai.wallet.datamanager.data.RtbJsonProtocol
import dispatch.{Http, url}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@RunWith(classOf[JUnitRunner])
class RtbAccess extends Specification with RtbJsonProtocol {

  val apiLocation = "http://rtb.karedo.co.uk:12339"
  val path = "/bids"

  def str(): String = {
    val rtb =
      Rtb(id = 2508744128L, timestamp = "2016-08-10T21:27:29.450Z", url = "http://datacratic.com/", language = "en", exchange = "mock", location = Location(countryCode = "CA", regionCode = "QC", cityName = "Montreal"), userIds = UserId(prov = 1568485738, xchg = 1475615520), imp = List(Impression(id = 1, formats = List("160x600")), Impression(id = 2, formats = List("160x600"))), spots = List(Spot(id = 1, formats = List("160x600"))))
    val json = rtb.toJson.toString
    json
  }
  "Test" should {
    "compose json" in {
      str must beEqualTo("""{"location":{"regionCode":"QC","cityName":"Montreal","metro":-1,"timezoneOffsetMinutes":-1,"dma":-1,"countryCode":"CA"},"timestamp":"2016-08-10T21:27:29.450Z","url":"http://datacratic.com/","exchange":"mock","id":2508744128,"language":"en","isTest":false,"userIds":{"prov":1568485738,"xchg":1475615520},"imp":[{"id":1,"formats":["160x600"],"position":0},{"id":2,"formats":["160x600"],"position":0}],"spots":[{"id":1,"formats":["160x600"],"position":0}]}""")
    }


    "we can call rtb" in {
      val request = url(s"$apiLocation$path")
        .POST
        .setBody(str())
        .setContentType("application/json","UTF8")

      val responseFuture = Http(request)

      val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
      response.getStatusCode mustEqual 200
      response.getResponseBody mustEqual "post called"
    }

  }

}