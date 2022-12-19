import sbt._
import Keys._

import io.scalac.sbt.processrunner.{ProcessRunnerPlugin, ProcessInfo}

import java.net.Socket

import scala.Console._
import scala.sys.process.ProcessBuilder

object Build extends Build {


  object restapi extends ProcessInfo {
    println("Registering restapi process")
    override def id: String = "restapi"
    val port: Int = 8090
    val host: String = "127.0.0.1"

    /* Starts simple nc server  */
    override def processBuilder: ProcessBuilder = {
      println(s"Starting rest on port " + port)
      Thread.sleep(3000)
      "java -Dconfig.resource=dummy.deployment.conf -jar data-manager-api/target/scala-2.11/data-manager-api-assembly-"+
        Common.globalVersion+".jar"
    }
    override def isStarted: Boolean = {
      try {
        new Socket("127.0.0.1", port).getInputStream.close()
        true
      } catch {
        case _: Exception => false
      }
    }
    override def applicationName: String = "restapi"
  }

}