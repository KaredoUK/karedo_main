import java.net.Socket

import scala.util.Try

object Embedder {


  val embedConnectionPort = 12345
  val embedMongoDBVersion =Version.Main.PRODUCTION

  lazy val network = new Net(embedConnectionPort, Network.localhostIsIPv6)

  lazy val mongodConfig = new MongodConfigBuilder().version(embedMongoDBVersion).net(network).build

  lazy val runtime = MongodStarter.getDefaultInstance

  lazy val mongodExecutable = runtime.prepare(mongodConfig)



  def startMongo : Unit = {

    println( "\tChecking if it is necessary to start mongo\n\n" )

    // See comment to post http://stackoverflow.com/questions/434718/sockets-discover-port-availability-using-java
    // ...It appears that as of Java 7, David Santamaria's answer doesn't work reliably any more.
    // It looks like you can still reliably use a Socket to test the connection, however.
    val socketToMongoServerTry = Try {
      new Socket("localhost", embedConnectionPort)
    }

    if(socketToMongoServerTry.isSuccess){
      println(s"NOT Starting server: Somebody already listening to port $embedConnectionPort")
    } else {
      socketToMongoServerTry.map { _.close() }

      println("Starting server as requested")
      mongodExecutable.start
    }
  }

  def stopMongo : Unit = {

    mongodExecutable.stop
  }
}
