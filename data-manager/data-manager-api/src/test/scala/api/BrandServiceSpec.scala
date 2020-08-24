package api

import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse}
import org.specs2.mutable.Specification
import parallelai.wallet.entity.Brand
import spray.client.pipelining._
import util.ApiHttpClientSpec

import java.util.UUID


class BrandServiceSpec extends ApiHttpClientSpec {
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  "Brand Service" >>  {
    "can create a new brand" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[BrandResponse]

      val newBrandUUID = UUID.randomUUID()
      mockedBrandDAO.insertNew(any[Brand]) returns Some(newBrandUUID)

      val response = wait(pipeline {
        Post( s"$serviceUrl/brand", BrandData("brand X", "iconpath"))
      })

      response shouldEqual BrandResponse(newBrandUUID)
    }

    "can retrieve a brand in the DB" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[BrandData]

      val brand =  new Brand(UUID.randomUUID(), "brandName", "brandIcon", List.empty)
      mockedBrandDAO.getById(any[UUID]) returns Some(brand)

      val response = wait(pipeline {
        Get( s"$serviceUrl/brand/${brand.id}")
      })

      response shouldEqual BrandData(brand.name, brand.iconPath)
    }
  }

}
