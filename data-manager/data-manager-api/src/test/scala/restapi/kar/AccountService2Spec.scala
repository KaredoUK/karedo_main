package restapi.kar

import java.util.UUID

import com.parallelai.wallet.datamanager.data.AccountAds
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => argEq}
import org.specs2.mutable.SpecificationLike
import org.specs2.runner.JUnitRunner
import restapi.AccountSuggestedOffersHttpService
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

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
