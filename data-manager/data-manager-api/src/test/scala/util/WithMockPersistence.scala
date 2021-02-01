package util

import akka.actor.ActorRef
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.typesafe.config.ConfigFactory
import core._
import parallelai.wallet.config.AppConfigPropertySource
import parallelai.wallet.persistence._
import restapi.Api
import web.Web


class WithMockPersistence(
  val servicePort: Int,
  override val brandDAO: BrandDAO,
  override val hintDAO: HintDAO,
  override val logDAO: LogDAO,
  override val clientApplicationDAO: ClientApplicationDAO,
  override val userAccountDAO: UserAccountDAO,
  override val mediaDAO: MediaDAO,
  override val offerDAO: OfferDAO,
  override val userSessionDAO: UserSessionDAO,
  override val saleDAO: SaleDAO,
  override val messenger: ActorRef)

  extends Injectable
  with BootedCore
  with Persistence
  with MessageActors
  with BaseCoreActors
{

  // Define The Configuration for the tests
  implicit def configProvider = AppConfigPropertySource(
    ConfigFactory.parseString(
      s"""
        |swagger = false
        |notification {
        |  email {
        |    auth.user.key = "email.auth.user.key"
        |    server.endpoint = "http://localhost/email"
        |    sender = "noreply@gokaredo.com"
        |  }
        |
        |  sms {
        |    actor.class="core.DummySMSActor"
        |    auth {
        |      user = "sms.usr"
        |      pwd = "sms.pwd"
        |      accesskey ="dummy"
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
        |    server.address = "http://localhost:$servicePort"
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
        | loggers = ["akka.event.slf4j.Slf4jLogger"]
        | loglevel = INFO
        |}
      """.stripMargin
    )
  )

  // Create a dependency injection module reading this configuration
  // don't use val otherwise you'll have a null pointer exception
  override implicit lazy val bindingModule : BindingModule = newBindingModuleWithConfig
}
