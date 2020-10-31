import java.io.IOException
import java.net.ServerSocket

import sbt._
import Keys._

object Embedder {

  import de.flapdoodle.embed.mongo._
  import de.flapdoodle.embed.mongo.config._
  import de.flapdoodle.embed.mongo.distribution._
  import de.flapdoodle.embed.process.runtime.Network


  val embedConnectionPort = 12345
  val embedMongoDBVersion =Version.Main.PRODUCTION

  lazy val network = new Net(embedConnectionPort, Network.localhostIsIPv6)

  lazy val mongodConfig = new MongodConfigBuilder().version(embedMongoDBVersion).net(network).build

  lazy val runtime = MongodStarter.getDefaultInstance

  lazy val mongodExecutable = runtime.prepare(mongodConfig)



  def startMongo : Unit = {
    var s:ServerSocket=null
    try {
      s=new ServerSocket(embedConnectionPort)
    } catch { case ioe: IOException => Unit }
    if(s==null){
      println(s"NOT Starting server: Somebody already listening to port $embedConnectionPort")
    } else {
      s.close()

      println("Starting server as requested")
      mongodExecutable.start
    }
  }

  def stopMongo : Unit = {

    mongodExecutable.stop
  }
}
