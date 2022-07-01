import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.ActorSystem
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, _}
import akka.http.scaladsl.{ConnectionContext, Http}
import akka.io.IO
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow


object Ssl {


  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  def sslContext: ConnectionContext = {
    println("Preparing SSLContext")
    val context = SSLContext.getInstance("TLS")
    val ks = KeyStore.getInstance("jks")
    val keyStoreResource = "/ssl-test-keystore.jks"
    val password = ""
    ks.load(getClass.getResourceAsStream(keyStoreResource), password.toCharArray)
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password.toCharArray)

    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(ks)
    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
    ConnectionContext.https(context)
  }




}
