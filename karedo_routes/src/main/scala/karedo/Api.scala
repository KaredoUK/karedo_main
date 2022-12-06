package karedo

import akka.http.scaladsl.{ConnectionContext, Http}
import akka.japi.Util
import com.typesafe.config.{Config, ConfigFactory}
import karedo.entity.{DbPrefs, DbUserAd}
import karedo.entity.dao.MongoConnection_Casbah
import karedo.routes.Routes
import karedo.util.{DefaultActorSystem, SSLSupport}


object Api
  extends MongoConnection_Casbah
  with Routes
  with DefaultActorSystem
  with SSLSupport {

  private val bindRoutes = routesWithLogging

  private val httpConfig = conf.getConfig("web.http")
  private val httpsConfig = conf.getConfig("web.https")

  if( httpConfig.getBoolean("enabled") ) {
    val host = httpConfig.getString("host")
    val port = httpConfig.getInt("port")

    println(s"Clear Binding on server $host and port $port")
    Http().bindAndHandle(bindRoutes, host, port, ConnectionContext.noEncryption())
  }

  if( httpsConfig.getBoolean("enabled") ) {
    val host = httpsConfig.getString("host")
    val port = httpsConfig.getInt("port")

    val keyStoreName = httpsConfig.getString("keystore.name")
    val keyStoreType = httpsConfig.getString("keystore.type")
    val keyStorePass = httpsConfig.getString("keystore.pass")

    println(s"SSL Binding on server $host and port $port")
    val https = getHttps(keyStoreName, keyStoreType, keyStorePass)
    Http().setDefaultServerHttpContext(https)
    Http().bindAndHandle(bindRoutes, host, port, connectionContext = https)
  }



//  if( doSsl ) {
//    println(s"SSL Binding on server $server and port $port")
//    val https = getHttps
//    Http().setDefaultServerHttpContext(https)
//    Http().bindAndHandle(bindRoutes, server, port, connectionContext = https)
//  } else {
//    println(s"Clear Binding on server $server and port $port")
//    Http().bindAndHandle(bindRoutes, server, port, ConnectionContext.noEncryption())
//  }

}

object Main extends App {
  Preload
  Api

}
