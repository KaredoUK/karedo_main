package restapi

import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => argEq}
import org.specs2.mutable.SpecificationLike
import org.specs2.runner.JUnitRunner
import spray.http.StatusCodes._
import spray.json.DefaultJsonProtocol
import spray.testkit.Specs2RouteTest
import sun.security.provider.MD5


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

  "simple GET account/getads" should {
    " getAds" in {

      val md5 = MD5.hash("dev1")
      val sessionId = UUID.randomUUID().toString
      val accountId = ""

      Post("/account/getads", AccountGetadsRequest(accountId, md5,sessionId)) ~> route ~> check {
        status === OK

        responseAs[AccountGetadsResponse] === AccountGetadsResponse(accountId,md5,sessionId,List("ad1","ad2"))
      }
    }
  }
}
