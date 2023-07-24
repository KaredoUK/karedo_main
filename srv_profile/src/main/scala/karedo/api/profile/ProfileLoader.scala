package karedo.api.account

import com.lightbend.lagom.scaladsl.client.{ConfigurationServiceLocatorComponents}
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server.{LagomApplication, LagomApplicationContext, LagomApplicationLoader}
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


class ProfileLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ProfileApplication(context) with ConfigurationServiceLocatorComponents //with DnsServiceLocator

//    {
//      override def serviceLocator: ServiceLocator = NoServiceLocator
//    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ProfileApplication(context) with LagomDevModeComponents

  override def describeServices = List(
    readDescriptor[ProfileService]
  )
}

abstract class ProfileApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[ProfileService](wire[ProfileServiceImpl])
}
