package karedo.entity.dao

import java.io._
import java.security._
import java.security.cert.{X509Certificate, _}
import javax.security.cert._
import java.util.{Base64, UUID}
import javax.net.ssl._

import com.mongodb.ServerAddress
import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}
import com.mongodb.casbah._
import karedo.trust.ReloadableX509TrustManager

object MongoConnectionObject_Casbah {
  // not found better way to have mongoinstance
  var instance: Option[MongoClient] = None
  val connectionsPerHost = 100

//  def getInstance(mongoHost: String, mongoPort: Int, mongoDbName: String, mongoDbUser: String, mongoDbPwd: String): MongoClient = {
//    def open = {
//      println(s"*** mongoHost: $mongoHost, mongoPort: $mongoPort")
//      val options = MongoClientOptions(connectionsPerHost = connectionsPerHost)
//
//      if (mongoDbUser.isEmpty) {
//        MongoClient(new ServerAddress(mongoHost, mongoPort), options = options)
//      } else {
//        MongoClient(new ServerAddress(mongoHost, mongoPort),
//          List(MongoCredential.createMongoCRCredential(mongoDbUser, mongoDbName, mongoDbPwd.toCharArray)),
//          options)
//      }
//    }
//
//    if (instance.isEmpty) instance = Some(open)
//
//    instance.get
//  }

  def getInstanceFromURI(mongoURI: String, mongoCACertB64: String = ""): MongoClient = {
    def open = {
      println(s"*** Mongo URL: $mongoURI")
      val options = MongoClientOptions(connectionsPerHost = connectionsPerHost)

      if(! mongoCACertB64.isEmpty) {
        println(s"Got Certificate\n${mongoCACertB64}")

        // trust everything
        // @TODO: Add proper CA checking in the future.
        trustAllChains
      }

      MongoClient(MongoClientURI(mongoURI))
    }

    if (instance.isEmpty) instance = Some(open)

    instance.get
  }

  def trustAllChains() = {
    // Create a trust manager that does not validate certificate chains
    val trustManager = new X509TrustManager {
      override def getAcceptedIssuers = null

      override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String) = {}

      override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String) = {}
    }

    val trustAllCerts: Array[TrustManager] = Array(trustManager)

    val allHostsValid: HostnameVerifier = new HostnameVerifier() {
      override def verify(hostname:String,  session:SSLSession): Boolean = true
    }

    try {
      val sc: SSLContext = SSLContext.getInstance("SSL")
      sc.init(null, trustAllCerts, new java.security.SecureRandom())
      SSLContext.setDefault(sc)
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory())
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}

trait MongoConnection_Casbah extends MongoConnectionConfig {
//  lazy val mongoClient = MongoConnectionObject_Casbah.getInstance(mongoHost, mongoPort, mongoDbName, mongoDbUser, mongoDbPwd)
  lazy val mongoClient = MongoConnectionObject_Casbah.getInstanceFromURI(mongoURI, mongoCACertB64)

//  lazy val db = mongoClient(mongoDbName)
  lazy val db = mongoClient.getDB(mongoDbName)

  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()

}

