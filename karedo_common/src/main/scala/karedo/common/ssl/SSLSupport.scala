package karedo.common.ssl

import java.security.SecureRandom
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.http.scaladsl.{ConnectionContext, HttpsConnectionContext}
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import karedo.common.akka.DefaultActorSystem
import karedo.common.config.Configurable
import karedo.common.crypto.KeySupport

//object Ssl {
//
//
//  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
//  // since we want non-default settings in this example we make a custom SSLContext available here
//  def sslContext(keyStoreName: String, keyStoreType: String ): ConnectionContext = {
//    println("Preparing SSLContext")
//    val context = SSLContext.getInstance("TLS")
//    val ks = KeyStore.getInstance(keyStoreType)
//    val keyStoreResource = keyStoreName
//    val password = ""
//    ks.load(getClass.getResourceAsStream(keyStoreResource), password.toCharArray)
//    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
//    keyManagerFactory.init(ks, password.toCharArray)
//
//    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
//    trustManagerFactory.init(ks)
//    context.init(keyManagerFactory.getKeyManagers, trustManagerFactory.getTrustManagers, new SecureRandom)
//    ConnectionContext.https(context)
//  }
//
//
//
//}

trait SSLSupport extends KeySupport with DefaultActorSystem with Configurable {
  val sslConfig = AkkaSSLConfig()

  def getHttps(): HttpsConnectionContext = {
    val keyManagerFactory: KeyManagerFactory = getDefaultKeyManagerFactory()
    val tmf: TrustManagerFactory = getDefaultTrustManagerFactory()

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    val https: HttpsConnectionContext = ConnectionContext.https(sslContext)

    https
  }
}



