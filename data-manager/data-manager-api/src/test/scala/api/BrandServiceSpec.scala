package api

import com.parallelai.wallet.datamanager.data._
import org.specs2.mutable.Specification
import parallelai.wallet.entity.{SuggestedAdForUsersAndBrandModel, Brand, AdvertisementDetail}
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
          SuggestedAdForUsersAndBrandModel(ad1,"ad1","iconId1"),
          SuggestedAdForUsersAndBrandModel(ad2,"ad2","iconId2"),
          SuggestedAdForUsersAndBrandModel(ad3,"ad3","iconId3")
        )



      val response = wait(pipeline {

        Get(s"$serviceUrl/account/$userId/brand/${brand.id}/ads?max=5")
      })

      response should beLike {
        case List(_,_,_) => ok
        case _ => ko
      }

    }


  }
}
