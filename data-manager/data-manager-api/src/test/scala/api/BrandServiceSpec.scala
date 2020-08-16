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

import akka.actor.ActorSystem
import parallelai.wallet.config.AppConfigPropertySource
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._

import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._

// Using this trait you just need to define the service actors as TestProbe and start the rest server
trait MockedServiceActors extends BootedCore with ServiceActors


// Using this trait you just need to define the DAO as mocked and start your rest server
trait MockedPersistence extends Injectable with BootedCore with Persistence with CoreActors {
  // Define The Configuration for the tests
  implicit def configProvider = AppConfigPropertySource(
    ConfigFactory.parseString(
      """
        |notification {
        |  email {
        |    auth.user.key = "email.auth.user.key"
        |    server.endpoint = "http://localhost/email"
        |    sender = "noreply@gokaredo.com"
        |  }
        |
        |  sms {
        |    auth {
        |      user = "sms.usr"
        |      pwd = "sms.pwd"
        |    }
        |
        |    sender = "Karedo"
        |
        |    server.endpoint = "http://localhost/sms/"
        |  }
        |}
        |
        |ui {
        |  web {
        |    server.address = "http://localhost:9000"
        |  }
        |}
      """.stripMargin
    )
  )

  // Create a dependency injection module reading this configuration
  override implicit val bindingModule : BindingModule = newBindingModuleWithConfig


}


class BrandServiceSpec extends Specification with NoTimeConversions {


  def wait[T](future: Future[T]): T = result(future, 20.seconds)

  implicit val system = ActorSystem()

  import system.dispatcher

  // execution context for futures
  val pipeline = {
    sendReceive ~> unmarshal[BrandResponse]
  }
  val url = "http://localhost:8080/"


  "Brand Service" >>  {
    "can create a new brand" in {

      val response = wait(pipeline {
        Post(url+"brand", BrandData("brand X", "iconpath"))
      })

      println("Returned UUID: "+response.id)

      true

    }
  }

}
