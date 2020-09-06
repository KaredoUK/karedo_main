package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{AdvertisementDetail, AdvertisementMetadata, Brand}


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
    val advDAO = new AdvMongoDAO
    val mongoClient = brandDAO.mongoClient

    def cleanCollection(name: String) = mongoClient.getDB("wallet_data").getCollection(name).remove(MongoDBObject.empty)

    def clean = {
      cleanCollection("Brand")
      cleanCollection("AdvertisementDetail")
    }


    def mybrand = Brand(UUID.randomUUID(),name = "brand X", iconId = "iconX", ads = List[AdvertisementMetadata]())

    "add two advertisings to brand and fetching them back" in {

      clean
      val brandId = brandDAO.insertNew(mybrand).get
      val text1: String = "adtext"
      val ad = AdvertisementDetail(text = text1, imageIds = List("image1","image2"), value = 100)
      val adId = advDAO.insertNew(ad).get

      val advertiseMeta = AdvertisementMetadata(adId, new DateTime)
      brandDAO.addAdvertisement(brandId, advertiseMeta)

      val text2: String = "adtext2"
      val ad2 = AdvertisementDetail(text = text2, imageIds = List("image3"), value = 200)
      val adId2 = advDAO.insertNew(ad2).get

      val advertiseMeta2 = AdvertisementMetadata(adId2, new DateTime)
      brandDAO.addAdvertisement(brandId, advertiseMeta2)

      val list = brandDAO.listAds(brandId)
      list.size must beEqualTo(2)
      val meta1=list(0)
      val ads1=advDAO.getById(meta1.detailId).get
      ads1.text must beEqualTo(text1)

      val meta2=list(0)
      val ads2=advDAO.getById(meta2.detailId).get
      ads2.text must beEqualTo(text1)

      brandDAO.delAdvertisement(brandId,meta2.detailId)
      print("deleted 2nd adv")
      true
    }
  }

}
