package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{AdvertisementDetail, Brand}


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
      text = text1, imageIds = List("image1", "image2"), value = 100)


    val text2 = "adtext2"
    val ad2 = AdvertisementDetail(
      text = text2, imageIds = List("image3"), value = 200)

    brandDAO.addAd(brandId, ad1)
    brandDAO.addAd(brandId, ad2)


    "counting ads for brand" in {
      val list = brandDAO.listAds(brandId)
      list.size must beEqualTo(2)
    }
    "get single ads" in {
      val ads1 = brandDAO.getAdById(ad1.id).get
      ads1.text must beEqualTo(text1)

      val ads2 = brandDAO.getAdById(ad2.id).get
      ads2.text must beEqualTo(text2)

      ads2.imageIds(0) must beEqualTo("image3")
      ads2.value must beEqualTo(200)
    }

    "delete an ad" in {
      brandDAO.delAd(ad2.id)

      val ads2bis = brandDAO.getAdById(ad2.id)
      ads2bis must beNone
    }
  }

}
