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
object RtbAccess extends Specification with RtbJsonProtocol {

  val apiLocation = "http://rtb.karedo.co.uk"
  val port1 = 12339
  val port2 = 12340

  val path1 = "/auctions"
  val path2 = "/win"

  def anAuction(): String = {

    Rtb(id = 2508744128L, timestamp = "2016-08-10T21:27:29.450Z", url = "http://datacratic.com/",
      language = "en", exchange = "mock",
      location = Location(countryCode = "CA", regionCode = "QC", cityName = "Montreal"),
      userIds = UserId(prov = "1568485738", xchg = "1475615520"),
      imp = List(
        Impression(id = 1, formats = List("160x600")),
        Impression(id = 2, formats = List("160x600"))
      ),
      spots = List(Spot(id = 1, formats = List("160x600")))
    ).toJson.toString
  }

  def aWin(): String = {
    // "account":["hello", "world"],
    // "adSpotId":"1", "auctionId":"3438835179", "bidTimestamp":0.0, "channels":[], "timestamp":1470865069.551620,
    // "type":1, "uids":{"prov":"2046608033", "xchg":"678906006"}, "winPrice":[62, "USD/1M"]}
    Win(account = List("hello", "world"), adSpotId = "1", auctionId = "3438835179", bidTimestamp = 0,
      channels = List(), timestamp = 1470865069.551620, atype = 1,
      uids = UserId(prov = "2046608033", xchg = "678906006"),
      winPrice = RtbCurrency(62, "USD/1M")
    ).toJson.toString
  }

  "Rtb layer" should {
    "* compose json for auction" in {
      val expected: String =
        """{"location":{"regionCode":"QC","cityName":"Montreal","metro":-1,
          |"timezoneOffsetMinutes":-1,"dma":-1,"countryCode":"CA"},
          |"timestamp":"2016-08-10T21:27:29.450Z","url":"http://datacratic.com/","exchange":"mock","id":2508744128,
          |"language":"en","isTest":false,
          |"userIds":{"prov":"1568485738","xchg":"1475615520"},
          |"imp":[{"id":1,"formats":["160x600"],"position":0},{"id":2,"formats":["160x600"],"position":0}],
          |"spots":[{"id":1,"formats":["160x600"],"position":0}]}""".stripMargin.replace("\n", "")
      anAuction() must beEqualTo(
        expected)
    }
    "* compose json for win" in {
      val s = aWin()

      val expected: String =
        """{"channels":[],"timestamp":1470865069.55162,"auctionId":"3438835179","adSpotId":"1",
          |"bidTimestamp":0,"uids":{"prov":"2046608033","xchg":"678906006"},"winPrice":[62,"USD/1M"],
          |"account":["hello","world"],"type":1}""".stripMargin.replace("\n", "")

      s must beEqualTo(expected)

    }


    "* POST an /auctions" in {
      val request = url(s"$apiLocation:$port1$path1")
        .POST
        .setBody(anAuction())
        .setContentType("application/json", "UTF8")
        .setHeader("X-Openrtb-Version", "2.1")

      val responseFuture = Http(request)

      val response = Await.result(responseFuture, Duration(100, TimeUnit.MILLISECONDS))
      response.getStatusCode mustEqual 204
      //response.getResponseBody mustEqual "post called"
    }
    "* POST a /win" in {
      val request = url(s"$apiLocation:$port2$path2")
        .POST
        .setBody(aWin())
        .setContentType("application/json", "UTF8")
        .setHeader("X-Openrtb-Version", "2.1")

      val responseFuture = Http(request)

      val response = Await.result(responseFuture, Duration(500, TimeUnit.MILLISECONDS))
      val content = response.getResponseBody
      response.getStatusCode mustEqual 200

    }

  }

}