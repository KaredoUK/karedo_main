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

import com.parallelai.wallet.datamanager.data.AccountAds

import scala.util.Try


@RunWith(classOf[JUnitRunner])
class AccountService2Spec
  extends AccountHttpService2
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


  "KAR-126 [prototype]" should {
    "/1 simple POST account/0/suggestedOffers returns valid sessionId" in {

      Post("/account/0/suggestedOffers", AccountSuggestedOffersRequest
      (deviceId = fixedDevIdMd5,sessionId = "")) ~>
        route ~>
        check {

        status === OK

        val AccountSuggestedOffersResponse(recvSessionId) = responseAs[AccountSuggestedOffersResponse]

        isUUID(recvSessionId) === true
        recvSessionId === fixedSessionId
      }
    }
    "/2 simple POST account/0/suggestedOffers with a sessionId" in {
      Post("/account/0/suggestedOffers", AccountSuggestedOffersRequest
      (deviceId = fixedDevIdMd5,sessionId = fixedSessionId)) ~>
        route ~>
        check {

          status === OK

          val AccountSuggestedOffersResponse(recvSessionId) = responseAs[AccountSuggestedOffersResponse]

          isUUID(recvSessionId) === true
          recvSessionId === fixedSessionId2
        }
    }
    "/3 simple POST account/xxxxx/suggestedOffers with a sessionId" in {
      Post("/account/"+fixedAccountId+"/suggestedOffers", AccountSuggestedOffersRequest
      (deviceId = fixedDevIdMd5,sessionId = fixedSessionId)) ~>
        route ~>
        check {

          status === OK

          val AccountSuggestedOffersResponse(recvSessionId) = responseAs[AccountSuggestedOffersResponse]

          isUUID(recvSessionId) === true
          recvSessionId === fixedSessionId2
        }
    }
  }
  def isUUID(x:String) = {
    Try (UUID.fromString(x)) match {
      case scala.util.Success(_) => true
      case _ => false
    }
  }
}
