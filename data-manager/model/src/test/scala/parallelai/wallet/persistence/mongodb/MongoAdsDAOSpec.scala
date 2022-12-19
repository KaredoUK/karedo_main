package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{SummaryImageDB, AdvertisementDetail, Brand}


class MongoAdsDAOSpec
  extends Specification
  with MongoTestUtils {

  val brandDAO=new BrandMongoDAO()

  brandDAO.dao.collection.remove(MongoDBObject())
  val aBrand=Brand(name="aBrand")


  sequential

  "Advertising handling" should {

    val brandId = brandDAO.insertNew(aBrand).get
    val text1 = "adtext"
    val ad1 = AdvertisementDetail(
      shortText = text1,
      detailedText = "long "+text1,
      termsAndConditions = "T&C",
      shareDetails ="share details",
      summaryImages = List(SummaryImageDB("imagea",1)),
      detailImages = List("image1", "image2"),
      karedos = 100,
      startDate=DateTime.now,
      endDate=DateTime.now.plusDays(10))


    val text2 = "adtext2"
    val ad2 = AdvertisementDetail(
      shortText = text2,
      detailedText = "long "+text2,
      termsAndConditions = "T&C",
      shareDetails = "share details",
      summaryImages = List(SummaryImageDB("imagex",1), SummaryImageDB("imagey",2)),
      detailImages = List("image3"),
      karedos = 200,
      startDate=DateTime.now,
      endDate=DateTime.now.plusDays(10))

    brandDAO.addAd(brandId, ad1)
    brandDAO.addAd(brandId, ad2)


    "counting ads for brand" in {
      val list = brandDAO.listAds(brandId)
      list.size must beEqualTo(2)
    }
    "get single ads" in {
      val ads1 = brandDAO.getAdById(ad1.id).get
      ads1.shortText must beEqualTo(text1)

      val ads2 = brandDAO.getAdById(ad2.id).get
      ads2.shortText must beEqualTo(text2)

      ads2.detailImages(0) must beEqualTo("image3")
      ads2.karedos must beEqualTo(200)
    }

    "delete an ad" in {
      brandDAO.delAd(ad2.id)

      val ads2bis = brandDAO.getAdById(ad2.id)
      ads2bis must beNone
    }
  }

}
