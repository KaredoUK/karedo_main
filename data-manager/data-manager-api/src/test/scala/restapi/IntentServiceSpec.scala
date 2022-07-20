package restapi

import java.util.UUID

import com.parallelai.wallet.datamanager.data.AccountAds
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => argEq}
import org.specs2.mutable.SpecificationLike
import org.specs2.runner.JUnitRunner
import spray.http.StatusCodes._
import spray.testkit.Specs2RouteTest

import scala.util.Try


@RunWith(classOf[JUnitRunner])
class IntentServiceSpec
  extends IntentHttpService
    with SpecificationLike
    with AccountAds
    with Specs2RouteTest {
  def actorRefFactory = system


  sequential

  "KAR-129 [prototype]" should {
    "list /intent/what" in {
      Get("/intent/what") ~> routeIntent ~> check {
        status === OK
        responseAs[List[String]] === List(
          "buy",
          "rent",
          "travel",
          "hire",
          "compare",
          "switch",
          "borrow",
          "visit"

        )
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
