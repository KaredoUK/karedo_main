package karedo.util

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.http.scaladsl.ConnectionContext


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
