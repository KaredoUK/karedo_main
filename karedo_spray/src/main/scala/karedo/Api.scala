package karedo

import akka.http.scaladsl.{ConnectionContext, Http}
import com.typesafe.config.{Config, ConfigFactory}
import karedo.sample.{DefaultActorSystem, Routes, Ssl}


object Api
  extends App
  with DefaultActorSystem {

  implicit val conf:Config = ConfigFactory.load()


  val doSsl=false
  val connContext = if(doSsl) Ssl.sslContext else ConnectionContext.noEncryption()
  Http().bindAndHandle(Routes.route,conf.getString("web.host"),conf.getInt("web.port"),connContext)
}
