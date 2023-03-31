package karedo.route

import akka.http.scaladsl.{ConnectionContext, Http}
import karedo.common.akka.DefaultActorSystem
import karedo.common.ssl.SSLSupport
import karedo.persist.entity.dao.MongoConnection_Casbah
import karedo.route.routes.Routes
import karedo.route.util.KaredoConstants


object Api
//  extends MongoConnection_Casbah
  extends Routes
  with DefaultActorSystem
  with SSLSupport
    with KaredoConstants
{

  private val bindRoutes = routesWithLogging


  if( httpConfig.getBoolean("enabled") ) {
    val host = httpConfig.getString("host")
    val port = httpConfig.getInt("port")

    println(s"Clear Binding on server $host and port $port")
    Http().bindAndHandle(bindRoutes, host, port, ConnectionContext.noEncryption())
  }

  if( httpsConfig.getBoolean("enabled") ) {
    val host = httpsConfig.getString("host")
    val port = httpsConfig.getInt("port")


    println(s"SSL Binding on server $host and port $port")
    val https = getHttps()
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
