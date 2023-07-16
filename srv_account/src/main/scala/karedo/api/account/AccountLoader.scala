package karedo.api.account.loader

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.typesafe.conductr.bundlelib.lagom.scaladsl.ConductRApplicationComponents
import com.lightbend.lagom.dns.DnsServiceLocator
import karedo.api.account.AccountService
import karedo.api.account.impl.AccountServiceImpl
import play.api.libs.ws.ahc.AhcWSComponents
import com.softwaremill.macwire._
import karedo.api.account.messages.RegisterRequest

import scala.collection.immutable


class AccountLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AccountApplication(context) with DnsServiceLocator

//    {
//      override def serviceLocator: ServiceLocator = NoServiceLocator
//    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AccountApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[AccountService]
  )
}

abstract class AccountApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[AccountService](wire[AccountServiceImpl])
}
