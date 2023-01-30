package karedo.util

import java.security.{KeyStore, PrivateKey, SecureRandom}

import com.typesafe.sslconfig.akka.AkkaSSLConfig
import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{KeyManagerFactory, SSLContext, TrustManagerFactory}

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route, RouteResult}
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
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

trait SSLSupport extends KeySupport with DefaultActorSystem with Configurable {
  val sslConfig = AkkaSSLConfig()

  def getHttps(keyStoreName: String, keyStoreType: String, keyStorePassword: String ): HttpsConnectionContext = {

    val ks = getKeyStore(keyStoreName, keyStoreType, keyStorePassword)

    val password: Array[Char] = keyStorePassword.toCharArray
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

trait KeySupport {
  def getKeyStore(keyStoreName: String, keyStoreType: String, keyStorePassword: String): KeyStore = {
    val password: Array[Char] = keyStorePassword.toCharArray

    val ks: KeyStore = KeyStore.getInstance(keyStoreType)
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream(keyStoreName)

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    ks
  }

  def getSecret(ks: KeyStore, alias: String, password: String): String = {
    val key = ks.getKey(alias, password.toCharArray).asInstanceOf[PrivateKey]
    println(s"Key : ${key.getAlgorithm} ${key.getFormat} ${key.getEncoded}")
    val secret = key.getEncoded.map("%02X" format _).mkString
    println("Secret: " + secret)
    secret
  }
}

