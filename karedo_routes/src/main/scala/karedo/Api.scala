package karedo

import akka.http.scaladsl.{ConnectionContext, Http}
import akka.japi.Util
import com.typesafe.config.{Config, ConfigFactory}
import karedo.entity.{DbPrefs, DbUserAd}
import karedo.entity.dao.MongoConnection
import karedo.routes.Routes
import karedo.util.{DefaultActorSystem, Ssl}


object Api
  extends MongoConnection
  with Routes
  with DefaultActorSystem {

  //Util.

  val doSsl=false
  val connContext = if(doSsl) Ssl.sslContext else ConnectionContext.noEncryption()
  private val server = conf.getString("web.host")
  private val port = conf.getInt("web.port")
  println(s"Binding on server $server and port $port")
  Http().bindAndHandle(routesWithLogging,server,port,connContext)
}
object Main extends App {
  Preload
  Api

}
