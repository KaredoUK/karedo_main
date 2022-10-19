package karedo.util

import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.http.scaladsl.ConnectionContext
import com.typesafe.sslconfig.akka.AkkaSSLConfig
import java.io.InputStream
import java.security.{ SecureRandom, KeyStore }
import javax.net.ssl.{ SSLContext, TrustManagerFactory, KeyManagerFactory }

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ RouteResult, Route, Directives }
import akka.http.scaladsl.{ ConnectionContext, HttpsConnectionContext, Http }
import akka.stream.ActorMaterializer
import com.typesafe.sslconfig.akka.AkkaSSLConfig

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

trait SSLSupport extends DefaultActorSystem with Configurable {
  val sslConfig = AkkaSSLConfig()

  def getHttps(keyStoreName: String, keyStoreType: String ): HttpsConnectionContext = {
    val password: Array[Char] = Array() // do not store passwords in code, read them from somewhere safe!

    val ks: KeyStore = KeyStore.getInstance(keyStoreType)
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream(keyStoreName)

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, password)

    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(ks)

    val sslContext: SSLContext = SSLContext.getInstance("TLS")
    sslContext.init(keyManagerFactory.getKeyManagers, tmf.getTrustManagers, new SecureRandom)
    val https: HttpsConnectionContext = ConnectionContext.https(sslContext)

    https
  }
}

