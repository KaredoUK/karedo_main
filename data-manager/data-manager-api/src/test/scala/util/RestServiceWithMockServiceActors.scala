package util

import akka.actor.ActorRef
import api.Api
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.typesafe.config.ConfigFactory
import core.{BootedCore, ServiceActors}
import parallelai.wallet.config.AppConfigPropertySource
import web.Web

// Using this trait you just need to define the service actors as TestProbe and start the rest server
class RestServiceWithMockServiceActors(
  val servicePort: Int,
  override val messenger: ActorRef,
  override val editAccount: ActorRef,
  override val brand: ActorRef,
  override val registration: ActorRef
) extends Injectable with BootedCore with ServiceActors with Api with Web {
  // Define The Configuration for the tests
  implicit def configProvider = AppConfigPropertySource(
    ConfigFactory.parseString(
      s"""
        |service {
        |   port = $servicePort
        |   bindAddress = "0.0.0.0"
        |}
      """.stripMargin
    )
  )

  // Create a dependency injection module reading this configuration
  // don't use val otherwise you'll have a null pointer exception
  override implicit lazy val bindingModule : BindingModule = newBindingModuleWithConfig
}
