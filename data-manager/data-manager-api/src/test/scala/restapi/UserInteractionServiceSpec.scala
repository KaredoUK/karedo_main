package restapi

import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => argEq}
import org.specs2.matcher.Matcher
import org.specs2.runner.JUnitRunner
import parallelai.wallet.entity._
import spray.client.pipelining._
import util.ApiHttpClientSpec

import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class UserInteractionServiceSpec
  extends ApiHttpClientSpec
  with RetryExamples {
  //with RestApiSpecMatchers only matchers used by account defined so not needed here 
  
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._
    
  sequential

  override def responseTimeout = 30.seconds


  "User Interaction Service" should {
    "PARALLELAI-108: User interaction with a brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[InteractionResponse]

      val brand = Brand(UUID.randomUUID(), "brandName", "iconID", List.empty)
      //mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val data=UserBrandInteraction(userId,brand.id,"share")
      val response = wait(pipeline {
        Post(s"$serviceUrl/user/"+userId.toString+"/interaction/brand", data).withHeaders(headers)
      })

      response.userId must beEqualTo(userId)
      response.userTotalPoints must beEqualTo(10)
    }
  }


}
