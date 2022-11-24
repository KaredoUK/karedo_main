package karedo.entity.dao

import com.mongodb.casbah.{MongoClient, MongoCredential}
import com.mongodb.ServerAddress
import com.mongodb.casbah.MongoClientOptions
import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}

object MongoConnection1Object {
  // not found better way to have mongoinstance
  var instance: Option[MongoClient] = None

  def getInstance(mongoHost: String, mongoPort: Int, mongoDbName: String, mongoDbUser: String, mongoDbPwd: String): MongoClient = {
    val pool = 1

    def open = {
      println(s"*** mongoHost: $mongoHost, mongoPort: $mongoPort")
      val options = MongoClientOptions(connectionsPerHost = 100)

      if (mongoDbUser.isEmpty) {
        MongoClient(new ServerAddress(mongoHost, mongoPort), options = options)
      } else {
        MongoClient(new ServerAddress(mongoHost, mongoPort),
          List(MongoCredential.createMongoCRCredential(mongoDbUser, mongoDbName, mongoDbPwd.toCharArray)),
          options)
      }
    }

    if (instance.isEmpty) instance = Some(open)

    instance.get
  }
}

trait MongoConnection1 extends MongoConnectionConfig {
  lazy val mongoClient1 = MongoConnection1Object.getInstance(mongoHost, mongoPort, mongoDbName, mongoDbUser, mongoDbPwd)

  lazy val db = mongoClient1(mongoDbName)
  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()

}

