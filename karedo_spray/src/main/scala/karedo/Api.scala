package karedo

import akka.http.scaladsl.{ConnectionContext, Http}
import com.typesafe.config.{Config, ConfigFactory}
import karedo.entity.DbPrefs
import karedo.entity.dao.MongoConnection
import karedo.sample.{DefaultActorSystem, Routes, Ssl}


object Api
  extends App
  with MongoConnection
  with Routes
  with DefaultActorSystem {

  val test = new DbPrefs {}
  val rows = test.preload()
  println(s"done loading $rows")



  val doSsl=false
  val connContext = if(doSsl) Ssl.sslContext else ConnectionContext.noEncryption()
  Http().bindAndHandle(routesWithLogging,conf.getString("web.host"),conf.getInt("web.port"),connContext)
}
