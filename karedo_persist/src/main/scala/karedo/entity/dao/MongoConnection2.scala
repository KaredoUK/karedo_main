package karedo.entity.dao

import org.mongodb.scala._
import org.slf4j.LoggerFactory

/**
  * Created by charaj on 12/01/2017.
  */

//  // Certficate form Compose
//  val certB64 = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURlekNDQW1PZ0F3SUJBZ0lFV0hOb1RqQU5CZ2txaGtpRzl3MEJBUTBGQURBL01UMHdPd1lEVlFRREREUmoKY21GcVlXaEFhMkZ5WldSdkxtTnZMblZyTFRCa016WTVOamhoWVRNNFlUSTRNelppWWpkbVpqa3haalZpTVRNeApZbUZrTUI0WERURTNNREV3T1RFd016a3hNRm9YRFRNM01ERXdPVEV3TURBd01Gb3dQekU5TURzR0ExVUVBd3cwClkzSmhhbUZvUUd0aGNtVmtieTVqYnk1MWF5MHdaRE0yT1RZNFlXRXpPR0V5T0RNMlltSTNabVk1TVdZMVlqRXoKTVdKaFpEQ0NBU0l3RFFZSktvWklodmNOQVFFQkJRQURnZ0VQQURDQ0FRb0NnZ0VCQU1UT3NqS2tVK3NZTnNLMgo3Z0g0dnI2UlZmSGg0ZDR5ZEZ2bVpBRFpEMDJ3dWU1NngycWdVSDFrclQvYzAzbllFeDNIV0M1MHpyT01ldWhmCm5iYVlmb0hXdGNsbFBMSGdXbUlMa2hjMXh5UG5UdVJiUCtjY0xITlBFdDNUOUwycjArSmZXVk5HK3ovZHlXVjYKZUdzUFlCU3hQd3hlTzVlQUtuSmxZQ0oxT0o1RlRLdEMyV25RYmZFSzMzcEJTQ2ZkUTdGQVBKaHZaeGNaYVlscgp4SGZIeXEvdWxpL1ppRm5vdlpudGJXTXFGRWJpeVhEWnNzemRNY2daL3Y2RDRHcnc3Ui83SnlTYWE5eDQ3ckxKCjVncFQ0RTZmT3JjSmRFdVg4U3JQUjFOdEkxVGk5cHNmelFTZEdhTGdlWGVQb0JtNWpRSU9JSzhoS21KUDBkQ1IKeEdMZElXRUNBd0VBQWFOL01IMHdIUVlEVlIwT0JCWUVGTDZkazh4Q2U1TklYa1V6UVdDeC80U0FUZ1g3TUE0RwpBMVVkRHdFQi93UUVBd0lDQkRBZEJnTlZIU1VFRmpBVUJnZ3JCZ0VGQlFjREFRWUlLd1lCQlFVSEF3SXdEQVlEClZSMFRCQVV3QXdFQi96QWZCZ05WSFNNRUdEQVdnQlMrblpQTVFudVRTRjVGTTBGZ3NmK0VnRTRGK3pBTkJna3EKaGtpRzl3MEJBUTBGQUFPQ0FRRUFNSlZlVzU4SGpGWmVtUmxoQkFybURQRVZYbkUrc3h6UG9ZdllneVhVYXVtQgptcThyblJoa2RVNjVxSHczVTFlMlBydnpkK3Rqd0ZVMkhOcEtpRlgzSW4yZGxvTk4wV0p3YjVERTR0alB1VzJGCjBLaU1reS9zWWs3R25hRTlydjQ3SXlIZkIwUFFKakNmblRQSU0rRC9Bb2Z4d0pXUXFVRXNkRVI1eVhsQTFtN2cKblhPSVljQUFBSjVCSTZUaURXa1ZxenplbHREN09Tc1RJcEZUVTkwZkNrTkErZm5rT21JMjd4QW85QWtXaVZJUQpQc2JYQkNPUW9zVEg3ZDREb3hJZElaVG9PQ3dWL2ZnUEsyMlVlMzhSTlN2Q3FQd3BjN0o0WmQwSDI4QS9DSWQwCmcyZ1o5UUFEL0hqZG9IVFRBUmtaWGNva3hndUhNS3VhNlNwRzNYdHJ4dz09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
//  val certBytes = java.util.Base64.getDecoder.decode(certB64)
//
//  val cert = javax.security.cert.X509Certificate.getInstance(certBytes)
//
//  //        val keyStore = KeyStore.getInstance("PKCS12")
//  //        keyStore.load(new ByteArrayInputStream(certBytes), "".toCharArray)
//  //
//  //        val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
//  //        keyManagerFactory.init(keyStore, "".toCharArray())
//
//  //        SSLSocketFactory
//  //
//  //        Security
//  //
//  //        com.mongodb.MongoCredential
//  //        com.mongodb.MongoCredentialsStore
//
//  val user: String = "CN=crajah@karedo.co.uk-0d36968aa38a2836bb7ff91f5b131bad"     // The x.509 certificate derived user name, e.g. "CN=user,OU=OrgUnit,O=myOrg,..."
//  val x509Credential = MongoCredential.createMongoX509Credential(user)
//
//  val SHA1Credential = MongoCredential.createScramSha1Credential("admin", "admin", "FNPQYDEFFCQDCGWX".toCharArray)
//
//  val serverAddress = List(new ServerAddress("sl-eu-lon-2-portal.2.dblayer.com", 16096), new ServerAddress("sl-eu-lon-2-portal.3.dblayer.com", 16096))
//  val mongoClient = MongoClient(serverAddress, List(SHA1Credential, x509Credential))
//
//
//  //        val uri=new MongoClientURI("mongodb://admin:FNPQYDEFFCQDCGWX@sl-eu-lon-2-portal.2.dblayer.com:16096,sl-eu-lon-2-portal.3.dblayer.com:16096/admin?ssl=true",
//  //        new com.mongodb.MongoClientOptions.Builder().socketFactory(SSLSocketFactory.getDefault()))
//  //        val mongoClient=MongoClient(uri)
//
//  //        val uri = com.mongodb.casbah.MongoClientURI("mongodb://admin:FNPQYDEFFCQDCGWX@sl-eu-lon-2-portal.2.dblayer.com:16096,sl-eu-lon-2-portal.3.dblayer.com:16096/admin?ssl=true")
//  //        val mongoClient = MongoClient(uri)
//
//
//  //        mongoClient
//
//  MongoClient(serverAddress, List(MongoCredential.createPlainCredential("karedo", "karedo", "K4r5d0DBM0ng0".toCharArray)))
//


object MongoConnection2Object extends MongoConnectionConfig {
  private var mongoClientInstance: Option[MongoClient] = None

  private val logger = LoggerFactory.getLogger("MongoConnection2")

  def getMongoClient: MongoClient = {
    def getMongoClientInner(count: Int): MongoClient = {
      mongoClientInstance match {
        case Some(mongoClient) => mongoClient
        case None => {
          mongoClientInstance = try {
            Some(MongoClient(mongoURL))
          } catch {
            case e: Exception => {
              logger.error(s"Unable to Create Mongo Client: Failure count: ${count}", e)

              None
            }
          }

          if( count > mongoRetryCount) throw new Exception(s"Failed ${mongoRetryCount} times to create Mongo Client.")

          getMongoClientInner(count + 1)
        }
      }
    }

    getMongoClientInner(0)
  }
}

trait MongoConnection2 extends MongoConnectionConfig {
  lazy val mongoClient = MongoConnection2Object.getMongoClient
  lazy val db = mongoClient.getDatabase(mongoDbName)
}
