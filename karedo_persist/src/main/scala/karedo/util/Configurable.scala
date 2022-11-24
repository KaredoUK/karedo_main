package karedo.util

import com.typesafe.config._
import spray.json._
import java.net.URL

trait Configurable extends ConfigObjectImplicits {
  import ConfigLoader._

  val conf = remoteConf
}

object ConfigLoader {
  println("Loading Local Config")
  val localConf = ConfigFactory.load()

  println("Loading Remote Config : " + localConf.getString("config.path"))
  val remoteConf = ConfigFactory.load(
    ConfigFactory.parseURL(
      new URL(
        localConf.getString("config.path")
      ), ConfigParseOptions.defaults().setSyntax(ConfigSyntax.CONF)
    )
  )

  println("Checking Remote Config - Version: " + remoteConf.getString("version"))

}

object ConfigObject {
  case class KaredoConfig(live:ComponentConfig, test: ComponentConfig)
  case class ComponentConfig(ios: ClientConfig, android: ClientConfig, web: ClientConfig, backend: BackendConfig)
  case class ClientConfig(api_base_url: String )
  case class BackendConfig(config_url: String)
}

trait ConfigObjectImplicits extends DefaultJsonProtocol {
  import ConfigObject._

  implicit val jason_BackendConfig = jsonFormat1(BackendConfig)
  implicit val jason_ClientConfig = jsonFormat1(ClientConfig)
  implicit val json_ComponentConfig = jsonFormat4(ComponentConfig)
  implicit val json_KaredoConfig:RootJsonFormat[KaredoConfig] = jsonFormat2(KaredoConfig)
}
