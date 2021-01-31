

import java.util.UUID

import org.specs2.mutable.Specification
import spray.httpx.UnsuccessfulResponseException


class TestingBrandInteraction
  extends Specification

  with ItEnvironment {

  // clearAll()

  sequential


  "Testing interaction brand " should {
    "add 10 points for sharing" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val gainedPoints = addBrandInteraction(r.sessionId, r.userId, b, "share", "facebook")

      gainedPoints === 10
    }
    "add 15 points for sharing and liking" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val gainedPoints = addBrandInteraction(r.sessionId, r.userId, b, "share", "facebook")

      val gainedPoints2 = addBrandInteraction(r.sessionId, r.userId, b, "like", "facebook")

      gainedPoints2 === 15

    }
    "giving exception if invalid brand" in {
      val r = RegisterAccount
      //val b = addBrand(r.sessionId, "C1")
      addBrandInteraction(r.sessionId, r.userId, UUID.randomUUID(), "share", "facebook") should
        throwAn[UnsuccessfulResponseException]
    }
  }

}
