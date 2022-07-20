package restapi

import java.util.UUID
import javax.ws.rs.Path

import com.wordnik.swagger.annotations.{ApiImplicitParams, ApiOperation, ApiResponses, _}
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => argEq}
import org.specs2.mutable.SpecificationLike
import org.specs2.runner.JUnitRunner
import spray.http.StatusCodes._
import spray.json.DefaultJsonProtocol
import spray.testkit.Specs2RouteTest
import sun.security.provider.MD5
import javax.ws.rs.Path

import akka.util.Timeout
import com.parallelai.wallet.datamanager.data.AccountAds

import scala.util.Try


@RunWith(classOf[JUnitRunner])
class AccountService2Spec
  extends AccountSuggestedOffersHttpService
    with SpecificationLike
    with AccountAds
    with Specs2RouteTest {
  def actorRefFactory = system

  object MD5 {
    def hash(s: String) = {
      val m = java.security.MessageDigest.getInstance("MD5")
      val b = s.getBytes("UTF-8")
      m.update(b, 0, b.length)
      new java.math.BigInteger(1, m.digest()).toString(16)
    }
  }
  sequential


  "KAR-127 [prototype]" should {
    "list /pref/names" in {
      Get("/pref/names") ~> routeAccountSuggestedOffers ~> check {
        status === OK

        responseAs[List[(String,String)]] === List(

          ("IAB22", "offers & discounts"),
          ("IAB18", "fashion & style"),
          ("IAB8", "food & drink"),
          ("IAB20", "travel & holidays"),
          ("IAB17", "sports"),
          ("IAB6", "family & children"),
          ("IAB7", "health & fitness"),
          ("IAB19", "computers & gadgets"),
          ("IAB4", "jobs & career"),
          ("IAB10", "home & garden"),
          ("IAB2", "cars & bikes"),
          ("IAB13", "personal finance"),
          ("IAB3", "business & finance"),
          ("IAB1", "arts & entertainment"),
          ("IAB14", "community & society"),
          ("IAB15", "science"),
          ("IAB16", "pets"),
          ("IAB5", "education"),
          ("IAB21", "property & housing"),
          ("IAB9", "hobbies & interests"),
          ("IAB11", "law, govt & politics"),
          ("IAB12", "news & current affairs"),
          ("IAB23", "religion & spirituality")
        )
      }
    }
  }

  "KAR-126 [prototype]" should {
    "/1 simple POST account/0/suggestedOffers returns valid sessionId" in {

      Post("/account/0/suggestedOffers", AccountSuggestedOffersRequest
      (deviceId = fixedDevIdMd5, sessionId = "")) ~>
        routeAccountSuggestedOffers ~>
        check {

          status === OK

          val AccountSuggestedOffersResponse(recvSessionId, list) = responseAs[AccountSuggestedOffersResponse]

          isUUID(recvSessionId) === true
          recvSessionId === fixedSessionId

          list === fixedListAds
        }
    }
    "/2 simple POST account/0/suggestedOffers with a sessionId" in {
      Post("/account/0/suggestedOffers", AccountSuggestedOffersRequest
      (deviceId = fixedDevIdMd5, sessionId = fixedSessionId)) ~>
        routeAccountSuggestedOffers ~>
        check {

          status === OK

          val AccountSuggestedOffersResponse(recvSessionId, list) = responseAs[AccountSuggestedOffersResponse]

          isUUID(recvSessionId) === true
          recvSessionId === fixedSessionId2

          list === fixedListAds
        }
    }
    "/3 simple POST account/xxxxx/suggestedOffers with a sessionId" in {
      Post("/account/" + fixedAccountId + "/suggestedOffers", AccountSuggestedOffersRequest
      (deviceId = fixedDevIdMd5, sessionId = fixedSessionId)) ~>
        routeAccountSuggestedOffers ~>
        check {

          status === OK

          val AccountSuggestedOffersResponse(recvSessionId, list) = responseAs[AccountSuggestedOffersResponse]

          isUUID(recvSessionId) === true
          recvSessionId === fixedSessionId2

          list === fixedListAds
        }
    }

  }

  def isUUID(x: String) = {
    Try(UUID.fromString(x)) match {
      case scala.util.Success(_) => true
      case _ => false
    }
  }
}
