import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import org.specs2.mutable.Specification

import scala.util.Random


class Testing
  extends Specification
  with RegistrationHelpers
  with BrandHelpers
  with MyUtility {

  clearAll()


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
      val b1=addBrand(r.sessionId,"company2")
      isUUID(b1.toString)

      val b1a1 = addAd(r.sessionId,b1,"ad company")

      val a1 = addAd(r.sessionId,b,"myad")
      isUUID(a1.toString)
      println(s"session: ${r.sessionId} brand: $b ad: $a1\n")

      val a2 = addAd(r.sessionId,b,"second ad")
      val a3 = addAd(r.sessionId,b,"third ad")

      val ads=listAds(r.sessionId,b)
      ads.size should_==(3)

      ads.filter(_.text=="second ad").size should_==(1)
      ads.filter(_.text=="4th ad").size should_==(0)

      val read2 = getAd(r.sessionId,b,a2.toString)
      read2.text should_==("second ad")

      val adscompany=listAds(r.sessionId,b1)
      adscompany.size should_==(1)

    }
  }
  "Associate user to brand and ads" in {
    val r = RegisterAccount
    val r2 = RegisterAccount
    val b = addBrand(r.sessionId,"C1")
    val b2 = addBrand(r.sessionId,"C2")
    val ad1 = addAd(r.sessionId,b,"AD1")
    val ad2 = addAd(r.sessionId,b,"AD2")
    val ad3 = addAd(r.sessionId,b,"AD3")
    val ad4 = addAd(r.sessionId,b,"AD4")
    val ad5 = addAd(r.sessionId,b,"AD5")

    addBrandToUser(r.sessionId, r.userId,b)
    addBrandToUser(r.sessionId, r.userId, b2)
    addBrandToUser(r2.sessionId, r2.userId,b2)

    val brands=listBrandsForUser(r.sessionId, r.userId)
    brands.size should_==(2)

    val brandsUser2=listBrandsForUser(r2.sessionId,r2.userId)
    brandsUser2.size should_==(1)

    val suggested=getSuggestedAds(r.sessionId, r.userId, b, 2)

    suggested.size should_==(2)
    suggested(0).text should_==("AD1")

    val suggested2=getSuggestedAds(r2.sessionId,r2.userId,b2,2)
    suggested2.size should_==(0)


  }
  "Media file" in {
    println("Testing media files")
    val r=RegisterAccount
    val originalB=Array[Byte](1,2,3,4)
    val id=addMedia(r.sessionId,"name","image/jpeg",originalB)
    //println("added media: "+id.mediaId)
    val hex = "[0-9a-f]+"
    id.mediaId must beMatching(hex)

    val b=getMedia(r.sessionId,id.mediaId)
    b should_==(originalB)

    implicit class ExtendedRandom(ran: scala.util.Random) {
      def nextByte = (ran.nextInt(256) - 128).toByte
    }
    val randomB=Array.fill(100)(scala.util.Random.nextByte)
    val id2=addMedia(r.sessionId,"name2","image/jpeg",randomB)
    id2.mediaId must beMatching(hex)

    val b2=getMedia(r.sessionId,id2.mediaId)
    b2 should_==(randomB)

  }

}
