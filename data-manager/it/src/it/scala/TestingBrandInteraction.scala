

import java.util.UUID

import com.parallelai.wallet.datamanager.data.{UserOfferInteraction, UserBrandInteraction}
import org.specs2.mutable.Specification
import rules.ComputePoints
import spray.httpx.UnsuccessfulResponseException


class TestingBrandInteraction
  extends Specification

  with ItEnvironment {

  clearAll()

  sequential


  "brand interaction " should {
    "add xxx points for sharing" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val int1=UserBrandInteraction(userId=r.userId, brandId=b,interaction="share", intType = "facebook")
      val gainedPoints = addBrandInteraction(r.sessionId, int1)

      gainedPoints === ComputePoints.GetInteractionPoints(int1)
    }
    "add yyy points for sharing and liking" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val int1=UserBrandInteraction(userId=r.userId, brandId=b,interaction="share", intType = "facebook")

      val gainedPoints = addBrandInteraction(r.sessionId, int1)

      val int2=UserBrandInteraction(userId=r.userId, brandId=b,interaction="like", intType = "facebook")

      val totalPoints2 = addBrandInteraction(r.sessionId, int2)

      totalPoints2 === ( ComputePoints.GetInteractionPoints(int1) +  ComputePoints.GetInteractionPoints(int2))

    }
    "giving exception if invalid brand" in {
      val r = RegisterAccount
      val int1=UserBrandInteraction(userId=r.userId, brandId=UUID.randomUUID(),interaction="share", intType = "facebook")
      addBrandInteraction(r.sessionId, int1) should
        throwAn[UnsuccessfulResponseException]
    }
  }
  "Offer interaction " should {
    "add ppp points for sharing" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val o = addAd(r.sessionId, b,"my ad")

      val int1=UserOfferInteraction(userId=r.userId, offerId=o,interaction="share", intType = "facebook")

      val gainedPoints = addOfferInteraction(r.sessionId, int1)

      gainedPoints === ComputePoints.GetInteractionPoints(int1)
    }
    "add qqq points for sharing and liking" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val o = addAd(r.sessionId, b,"my ad")

      val int1=UserOfferInteraction(userId=r.userId, offerId=o,interaction="share", intType = "facebook")

      val gainedPoints = addOfferInteraction(r.sessionId, int1)

      val int2=UserOfferInteraction(userId=r.userId, offerId=o,interaction="like", intType = "facebook")


      val totalPoints2 = addOfferInteraction(r.sessionId, int2)

      totalPoints2 === ( ComputePoints.GetInteractionPoints(int1) +  ComputePoints.GetInteractionPoints(int2))

    }
    "giving exception if invalid offer" in {
      val r = RegisterAccount
      //val b = addBrand(r.sessionId, "C1")
      val int1=UserOfferInteraction(userId=r.userId, offerId=UUID.randomUUID(),interaction="share", intType = "facebook")

      addOfferInteraction(r.sessionId, int1) should
        throwAn[UnsuccessfulResponseException]
    }
  }
  "Offer sales" should {
    "Get code" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId, "C1")
      val offer = addAd(r.sessionId, b, "myad")
      val code = getOfferCode(r,offer)
      code.length === 8
    }
  }

}
