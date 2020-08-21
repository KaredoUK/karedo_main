package api


import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.config.ConfigPropertySource
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import com.parallelai.wallet.datamanager.data.{BrandData, BrandResponse}
import com.typesafe.config.ConfigFactory
import core.{Persistence, CoreActors, ServiceActors, BootedCore}

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions

import akka.actor.{ActorRef, ActorSystem}
import parallelai.wallet.config.AppConfigPropertySource
import parallelai.wallet.entity.Brand
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import spray.client.pipelining._
import util.RestServiceWithMockPersistence
import web.Web

import java.util.UUID
import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._

import org.specs2.mock._

class BrandServiceSpec extends Specification with NoTimeConversions with Mockito {
  // Problems acting on same mocks when running in parallel
  sequential

  import parallelai.wallet.util.SprayJsonSupport._


  def wait[T](future: Future[T]): T = result(future, 20.seconds)

  implicit val system = ActorSystem("testClient")

  // execution context for futures
  import system.dispatcher

  val serviceUrl = "http://localhost:8080"

  val mockedBrandDAO = mock[BrandDAO]
  val mockedClientApplicationDAO = mock[ClientApplicationDAO]
  val mockedUserAccountDAO = mock[UserAccountDAO]

  val mockedServer = new RestServiceWithMockPersistence(mockedBrandDAO, mockedClientApplicationDAO, mockedUserAccountDAO)

  "Brand Service" >>  {
    "can create a new brand" in {
      val pipeline = sendReceive ~> unmarshal[BrandResponse]

      val newBrandUUID = UUID.randomUUID()
      mockedBrandDAO.insertNew(any[Brand]) returns Some(newBrandUUID)

      val response = wait(pipeline {
        Post( s"$serviceUrl/brand", BrandData("brand X", "iconpath"))
      })

      response shouldEqual BrandResponse(newBrandUUID)
    }

    "can retrieve a brand in the DB" in {
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
