package restapi


import com.parallelai.wallet.datamanager.data._
import org.specs2.matcher.Matcher
import org.specs2.mutable.Specification
import parallelai.wallet.entity._
import spray.client.pipelining._
import util.{ RestApiSpecMatchers, ApiHttpClientSpec }
import java.util.UUID
import scala.concurrent.duration._
import org.mockito.Matchers.{ eq => argEq }
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import java.util.Date
import org.joda.time.DateTime
import spray.http.HttpHeaders.RawHeader
import org.specs2.mock.Mockito



@RunWith(classOf[JUnitRunner])
class BrandServiceSpec
  extends ApiHttpClientSpec
  with RetryExamples {
  //with RestApiSpecMatchers only matchers used by account defined so not needed here 
  
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._
    
  sequential

  override def responseTimeout = 30.seconds

  "Brand Service" should {
    "PARALLELAI-67: Create Brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[BrandResponse]

      val newBrandUUID = UUID.randomUUID()
      mockedBrandDAO.insertNew(any[Brand]) returns Some(newBrandUUID)

      val data: BrandData = BrandData("brand X", iconId="iconID")

      val response = wait(pipeline {
        Post(s"$serviceUrl/brand", data).withHeaders(headers)
      })

      response shouldEqual BrandResponse(newBrandUUID)

      def m: Matcher[Brand] =
        ({b: Brand =>
          b.name == data.name && b.iconId == data.iconId
        },
          s"Brand should have been inserted with values")

      there was one(mockedBrandDAO).insertNew(argThat(m))
    }

    "PARALLELAI-95: Get a single brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[BrandData]

      val brand = new Brand(UUID.randomUUID(), "brandName", iconId="iconID", ads=List.empty)
      mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val response = wait(pipeline {
        Get(s"$serviceUrl/brand/${brand.id}").withHeaders(headers)
      })

      // this has issues with dates because they are created with different values
      response.name shouldEqual brand.name
      response.iconId shouldEqual brand.iconId
    }

    "PARALLELAI-68: Deactivate Brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[String]

      val brand = new Brand(UUID.randomUUID(), "brandName", iconId="iconID", ads=List.empty)
      mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val response = wait(pipeline {
        Delete(s"$serviceUrl/brand/${brand.id}").withHeaders(headers)
      })

      there was one(mockedBrandDAO).delete(brand.id)
    }

    "PARALLELAI-65: Create Ad" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[AdvertDetailListResponse]

      val brand = new Brand(UUID.randomUUID(), "brandName", iconId="iconID", ads=List.empty)
      // mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val ad = AdvertDetailApi(
        shortText = "text",
        detailedText="detailed",
        termsAndConditions="T&C",
        shareDetails="share details",
        summaryImages = List(),
        imageIds = List(ImageId("image1"), ImageId("image2")),
        karedos = 100)

      val response = wait(pipeline {

        Post(s"$serviceUrl/brand/${brand.id}/advert", ad).withHeaders(headers)
      })

      response should beLike {
        case AdvertDetailListResponse(
        _, ad.shortText, _) => ok
        case _ => ko
      }
      print(s"response.id: ${response.offerId}")
      there was one(mockedBrandDAO).addAd(
        brand.id,
        AdvertisementDetail(id=any[UUID],
          publishedDate=any[DateTime],
          startDate=DateTime.now,
          endDate=DateTime.now.plusDays(10),
          shortText=ad.shortText,
          detailedText=ad.detailedText,
          termsAndConditions=ad.termsAndConditions,
          shareDetails=ad.shareDetails,
          summaryImages=ad.summaryImages.map(api => SummaryImageDB(api.imageId,api.imageType)),
          detailImages=ad.imageIds.map {_.imageId},
          karedos=ad.karedos
        )
      )

    }

    /*"PARALLELAI-59: Get Next N Ads For User For Brand" in new WithMockedPersistenceRestService {
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
*/

  }



}
