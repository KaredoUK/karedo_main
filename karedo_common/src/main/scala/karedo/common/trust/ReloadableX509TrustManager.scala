package karedo.common.trust

import java.io._
import java.security.{KeyStore, NoSuchAlgorithmException}
import java.security.cert.Certificate
import java.security.cert.{CertificateException, X509Certificate, _}
import java.util.{Base64, UUID}
import javax.net.ssl.{TrustManagerFactory, X509TrustManager, _}

/**
  * Created by charaj on 24/01/2017.
  */
class ReloadableX509TrustManager(path_to_trust_store: String, password: String) extends X509TrustManager { self =>
  var trustStorePath:String = path_to_trust_store
  var trustManager:X509TrustManager = _
  var tempCertList:List[Certificate] = List()
  var pwd = password.toCharArray


  override def getAcceptedIssuers = trustManager.getAcceptedIssuers

  override def checkClientTrusted(x509Certificates: Array[X509Certificate], authType: String) = {
    trustManager.checkClientTrusted(x509Certificates, authType)
  }

  override def checkServerTrusted(x509Certificates: Array[X509Certificate], authType: String) = {
    try
      trustManager.checkServerTrusted(x509Certificates, authType)
    catch {
      case e: CertificateException => {
        addServerCertAndReload(x509Certificates(0), true)
        trustManager.checkServerTrusted(x509Certificates, authType)
      }
    }
  }

  def reloadTrustManager() = {
    // load keystore from specified cert store (or default)
    val keystore = KeyStore.getInstance(KeyStore.getDefaultType)

    try {
      val in = new FileInputStream(trustStorePath)
      keystore.load(in, pwd)
      in.close()
    } catch {
      case e:Exception => {
        val password = UUID.randomUUID
        keystore.load(null, pwd)
        val os = new FileOutputStream(trustStorePath)
        keystore.store(os, pwd)
        os.flush()
        os.close()

        val in = new FileInputStream(trustStorePath)
        keystore.load(in, pwd)
        in.close()
      }
    }

    // add all temporary certs to KeyStore (ts)
    tempCertList foreach(cert => {
      keystore.setCertificateEntry(UUID.randomUUID.toString, cert)
      val newEntry:KeyStore.Entry = new KeyStore.TrustedCertificateEntry(cert)
      keystore.setEntry(UUID.randomUUID.toString, newEntry, null)
    })

    // initialize a new TMF with the Keystore we just loaded
    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
    tmf.init(keystore)

    // acquire X509 trust manager from factory
    val tms = tmf.getTrustManagers

    val tmsX509s = tms.filter(_ match {
      case t: X509TrustManager => true
      case _ => false
    }).toList match {
      case h::t => trustManager = h.asInstanceOf[X509TrustManager]
      case Nil => throw new NoSuchAlgorithmException("No X509TrustManager in TrustManagerFactory")
    }
  }

  def addServerCertAndReload(cert: Certificate, permanent: Boolean) = {
    try {
      if (permanent) {
        Runtime.getRuntime.exec(s"keytool -import ")
      } else {
        tempCertList = tempCertList ++ List(cert)
      }
      reloadTrustManager
    }
    catch {
      case e: Exception => e.printStackTrace()
    }
  }

  def getCertFromB64(certB64: String): Certificate = {
    val certBytes:Array[Byte] = Base64.getDecoder.decode(certB64)
    val cf = CertificateFactory.getInstance("X.509")
    val cin = new ByteArrayInputStream(certBytes)
    val cert = cf.generateCertificate(cin)
    cin.close()
    cert
  }
}
