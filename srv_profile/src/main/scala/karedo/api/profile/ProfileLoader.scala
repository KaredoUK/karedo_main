package karedo.api.account

import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


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
