package karedo

import akka.http.scaladsl.{ConnectionContext, Http}
import akka.japi.Util
import com.typesafe.config.{Config, ConfigFactory}
import karedo.entity.{DbPrefs, DbUserAd}
import karedo.entity.dao.MongoConnection
import karedo.routes.Routes
import karedo.util.{DefaultActorSystem, SSLSupport, Ssl}


object Api
  extends MongoConnection
  with Routes
  with DefaultActorSystem
  with SSLSupport {

  //Util.

  private val doSsl = conf.getBoolean("web.ssl")
  private val server = conf.getString("web.host")
  private val port = conf.getInt("web.port")

  private val bindRoutes = routesWithLogging


  if( doSsl ) {
    println(s"SSL Binding on server $server and port $port")
    val https = getHttps
    Http().setDefaultServerHttpContext(https)
    Http().bindAndHandle(bindRoutes, server, port, connectionContext = https)
  } else {
    println(s"Clear Binding on server $server and port $port")
    Http().bindAndHandle(bindRoutes, server, port, ConnectionContext.noEncryption())
  }

}

object Main extends App {
  Preload
  Api

}
