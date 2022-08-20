package karedo

import akka.http.scaladsl.{ConnectionContext, Http}
import com.typesafe.config.{Config, ConfigFactory}
import karedo.entity.{DbPrefs, DbUserAd}
import karedo.entity.dao.MongoConnection
import karedo.routes.Routes
import karedo.util.{DefaultActorSystem, Ssl}


object Api
  extends App
  with MongoConnection
  with Routes
  with DefaultActorSystem {

  val prefs = new DbPrefs {}
  val rows = prefs.preload()
  println(s"DbPrefs done loading $rows")

  val ads = new DbUserAd {}
  println(s"DbUserAd done loading $rows")



  val doSsl=false
  val connContext = if(doSsl) Ssl.sslContext else ConnectionContext.noEncryption()
  Http().bindAndHandle(routesWithLogging,conf.getString("web.host"),conf.getInt("web.port"),connContext)
}
