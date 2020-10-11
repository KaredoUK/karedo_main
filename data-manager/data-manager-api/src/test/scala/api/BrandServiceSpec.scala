package api

import com.parallelai.wallet.datamanager.data._
import org.specs2.mutable.Specification
import parallelai.wallet.entity.{Hint, SuggestedAdForUsersAndBrandModel, Brand, AdvertisementDetail}
import spray.client.pipelining._
import util.{ RestApiSpecMatchers, ApiHttpClientSpec }
import java.util.UUID
import scala.concurrent.duration._
import org.mockito.Matchers.{ eq => argEq }
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.util.Date
import org.joda.time.DateTime

@RunWith(classOf[JUnitRunner])
class BrandServiceSpec extends ApiHttpClientSpec with RestApiSpecMatchers {
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  override def responseTimeout = 30.seconds

  "Brand Service" should {
    "PARALLELAI-67API: Create Brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[BrandResponse]

      val newBrandUUID = UUID.randomUUID()
      mockedBrandDAO.insertNew(any[Brand]) returns Some(newBrandUUID)

      val response = wait(pipeline {
        Post(s"$serviceUrl/brand", BrandData("brand X", "iconID"))
      })

      response shouldEqual BrandResponse(newBrandUUID)
    }

    "PARALLELAI 95 API: Get a single brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[BrandData]

      val brand = new Brand(UUID.randomUUID(), "brandName", "iconID", List.empty)
      mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val response = wait(pipeline {
        Get(s"$serviceUrl/brand/${brand.id}")
      })

      response shouldEqual BrandData(brand.name, brand.iconId)
    }

    "PARALLELAI-68API: Deactivate Brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[String]

      val brand = new Brand(UUID.randomUUID(), "brandName", "iconID", List.empty)
      mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val response = wait(pipeline {
        Delete(s"$serviceUrl/brand/${brand.id}")
      })

      there was one(mockedBrandDAO).delete(brand.id)
    }

    "PARALLELAI-65API: Create Ad" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[AdvertDetailResponse]

      val brand = new Brand(UUID.randomUUID(), "brandName", "iconID", List.empty)
      // mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val ad = AdvertDetail(text = "text", imageIds = List("image1", "image2"), value = 100)

      val response = wait(pipeline {

        Post(s"$serviceUrl/brand/${brand.id}/advert", ad)
      })

      response should beLike {
        case AdvertDetailResponse(_, ad.text, ad.imageIds, ad.value) => ok
        case _ => ko
      }
      print(s"response.id: ${response.id}")
      there was one(mockedBrandDAO).addAd(brand.id, AdvertisementDetail(id=any[UUID],publishedDate=any[DateTime], text=ad.text,imageIds=ad.imageIds,value=ad.value))

    }
    "PARALLELAI-59API: Get Next N Ads For User For Brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[List[SuggestedAdForUsersAndBrand]]

      val brand = new Brand(UUID.randomUUID(), "brandName", "iconID", List.empty)
      // mockedBrandDAO.getById(any[UUID]) returns Some(brand)
      val userId=UUID.randomUUID()

      val ad1=UUID.randomUUID()
      val ad2=UUID.randomUUID()
      val ad3=UUID.randomUUID()

      mockedHintDAO.suggestedNAdsForUserAndBrandLimited(userId,brand.id,5)  returns
        List(
          Hint(userId=userId,brandId=brand.id, ad=ad1, score=0.5),
          Hint(userId=userId,brandId=brand.id, ad=ad2, score=0.6),
          Hint(userId=userId,brandId=brand.id, ad=ad3, score=0.7)
        )

      mockedBrandDAO.getAdById(ad1) returns Some(AdvertisementDetail(ad1, text="text1"))
      mockedBrandDAO.getAdById(ad2) returns Some(AdvertisementDetail(ad2, text="text2"))
      mockedBrandDAO.getAdById(ad3) returns Some(AdvertisementDetail(ad3, text="text3"))

      val response = wait(pipeline {

        Get(s"$serviceUrl/account/$userId/brand/${brand.id}/ads?max=5")
      })

      response must have size(3)
      response(0) match {
        case SuggestedAdForUsersAndBrand(ad1,"text1",_) => ok
        case _ => ko
      }

      response(1) match {
        case SuggestedAdForUsersAndBrand(ad2,"text2",_) => ok
        case _ => ko
      }

      response(2) match {
        case SuggestedAdForUsersAndBrand(ad2,"text3",_) => ok
        case _ => ko
      }




    }


  }
}
