package karedo.common.crypto

import java.io.InputStream
import java.security.{KeyStore, MessageDigest, PrivateKey}
import javax.net.ssl.{KeyManagerFactory, TrustManagerFactory}

/**
  * Created by charaj on 17/05/2017.
  */
trait KeySupport {
  private val keyStoreName = "karedo_keystore.jks"
  private val keyStoreType = "jks"
  private val keyStorePass = "K4r5d0Key"
  private val keyAlias     = "karedo.co.uk"

  def getKeyStore(keyStoreName: String, keyStoreType: String, keyStorePassword: String): KeyStore = {
    val password: Array[Char] = keyStorePassword.toCharArray

    val ks: KeyStore = KeyStore.getInstance(keyStoreType)
    val keystore: InputStream = getClass.getClassLoader.getResourceAsStream(keyStoreName)

    require(keystore != null, "Keystore required!")
    ks.load(keystore, password)

    ks
  }

  def getSecret(ks: KeyStore, alias: String, password: String): Array[Byte] = {
    val key = ks.getKey(alias, password.toCharArray).asInstanceOf[PrivateKey]
    MessageDigest.getInstance("SHA-256").digest(key.getEncoded)
      //.map("%02X" format _).mkString
  }

  lazy val getDefaultSecret = getSecret(getDefaultKeyStore, keyAlias, keyStorePass)
  lazy val getDefaultKeyStore = getKeyStore(keyStoreName, keyStoreType, keyStorePass)

  def getDefaultKeyManagerFactory() = {
    val password: Array[Char] = keyStorePass.toCharArray
    val keyManagerFactory: KeyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(getDefaultKeyStore, password)

    keyManagerFactory
  }

  def getDefaultTrustManagerFactory() = {
    val tmf: TrustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    tmf.init(getDefaultKeyStore)

    tmf
  }
}

