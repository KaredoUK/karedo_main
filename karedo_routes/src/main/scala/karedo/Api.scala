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
  Http().bindAndHandle(routesWithLogging,conf.getString("web.host"),conf.getInt("web.port"),connContext)
}
object Main extends App {
  Preload
  Api

}
