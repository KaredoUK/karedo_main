package util

import akka.actor.ActorRef
import api.Api
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.typesafe.config.ConfigFactory
import core._
import parallelai.wallet.config.AppConfigPropertySource
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import web.Web

import scala.util.Random

// Using this trait you just need to define the DAO as mocked and start your rest server
class RestServiceWithMockPersistence(
  val servicePort: Int,
  override val brandDAO: BrandDAO,
  override val clientApplicationDAO: ClientApplicationDAO,
  override val userAccountDAO: UserAccountDAO) extends Injectable with BootedCore with Persistence with BaseCoreActors with MessageActors with Api with Web {


  // Define The Configuration for the tests
  implicit def configProvider = AppConfigPropertySource(
    ConfigFactory.parseString(
      s"""
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
        |
        |service {
        |   port = $servicePort
        |   bindAddress = "0.0.0.0"
        |}
        |
        |akka {
        | log-dead-letters-during-shutdown = off
        | loglevel = DEBUG
        | loggers = ["akka.event.slf4j.Slf4jLogger"]
        |}
      """.stripMargin
    )
  )

  // Create a dependency injection module reading this configuration
  // don't use val otherwise you'll have a null pointer exception
  override implicit lazy val bindingModule : BindingModule = newBindingModuleWithConfig

  override val messenger: ActorRef = ActorRef.noSender
}
