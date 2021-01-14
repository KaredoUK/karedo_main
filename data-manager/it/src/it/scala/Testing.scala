import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import org.specs2.mutable.Specification



class Testing
  extends Specification
  with RegistrationHelpers
  with BrandHelpers
  with MyUtility {


  "Testing all data-server-api" should {
    "UserRegistrationBlock" in {

      val r = RegisterAccount

      isUUID(r.application.toString)
      isUUID(r.userId.toString)
      isUUID(r.sessionId)



    }
    "Reset application" in {
      val (applicationId, activationCode0, activationCode) = ResetAccount

      activationCode0 must be_!==(activationCode)
    }
    "Create brand and ads" in {
      val r = RegisterAccount
      val b = addBrand(r.sessionId,"pakkio")
      isUUID(b.toString)
      val a1 = addAd(r.sessionId,b,"myad")
      isUUID(a1.toString)
      println(s"session: ${r.sessionId} brand: $b ad: $a1\n")

      val a2 = addAd(r.sessionId,b,"second ad")
      val a3 = addAd(r.sessionId,b,"third ad")

      val ads=listAds(r.sessionId,b)
      ads.size should_==(3)

      ads.filter(_.text=="second ad").size should_==(1)
      ads.filter(_.text=="4th ad").size should_==(0)


      1==1
    }
  }

}
