package karedo.util

import com.typesafe.config._
import spray.json._
import java.net.URL

import org.slf4j.LoggerFactory
import org.slf4j.Logger

import scala.collection.JavaConverters._

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

  val envMap = ConfigFactory.systemEnvironment().entrySet().asScala.map { e => (e.getKey.toString, e.getValue.toString) }
  val propMap = ConfigFactory.systemProperties().entrySet().asScala.map { e => (e.getKey.toString, e.getValue.toString) }
  val remMap = remoteConf.entrySet().asScala.map { e => (e.getKey.toString, e.getValue.toString) }

  val allMap:Map[String, String] = (envMap ++ propMap ++ remMap).toMap

  val allConf = ConfigFactory.parseMap(allMap.asJava)

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
