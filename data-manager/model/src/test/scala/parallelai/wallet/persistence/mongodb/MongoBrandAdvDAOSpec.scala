package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{AdvertisementDetail, Brand}


/**
 * Created by pakkio on 29/09/2014.
 */
class MongoBrandAdvDAOSpec extends Specification with NoTimeConversions with MongoTestUtils {

  sequential

  "BrandMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> "27017",
        "mongo.db.name" -> "wallet_data",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )
    //

    val brandDAO = new BrandMongoDAO

    val mongoClient = brandDAO.mongoClient

    def cleanCollection(name: String) = mongoClient.getDB("wallet_data").getCollection(name).remove(MongoDBObject.empty)

    def clean = {
      cleanCollection("Brand")
      cleanCollection("AdvertisementDetail")
    }


    def mybrand = Brand(UUID.randomUUID(),name = "brand X", iconId = "iconX", ads = List[AdvertisementDetail]())

    "add two advertisings to brand and fetching them back" in {

      clean
      val brandId = brandDAO.insertNew(mybrand).get


      val text1= "adtext"
      val ad1 = AdvertisementDetail(
        text = text1, imageIds = List("image1","image2"), value = 100)


      val text2= "adtext2"
      val ad2 = AdvertisementDetail(
        text = text2, imageIds = List("image3"), value = 200)

      brandDAO.addAd(brandId, ad1)
      brandDAO.addAd(brandId, ad2)


      val list = brandDAO.listAds(brandId)
      list.size must beEqualTo(2)

      val ads1=brandDAO.getAdById(ad1.id).get
      ads1.text must beEqualTo(text1)



      val ads2=brandDAO.getAdById(ad2.id).get
      ads2.text must beEqualTo(text2)
      ads2.imageIds(0) must beEqualTo("image3")
      ads2.value must beEqualTo(200)

      brandDAO.delAd(ads2.id)

      val ads2bis=brandDAO.getAdById(ad2.id)
      ads2bis must beNone

    }
  }

}
