package karedo.entity.dao

import com.mongodb.casbah.{MongoClient, MongoCredential}
import com.mongodb.ServerAddress
import com.mongodb.casbah.MongoClientOptions
import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}
import com.typesafe.config.Config

import org.joda.time.{DateTime, DateTimeZone}

trait MongoConnection extends Configurable {



  lazy val mongoHost = conf.getString("mongo.server.host")
  lazy val mongoPort = conf.getInt("mongo.server.port")
  lazy val mongoDbName = conf.getString("mongo.db.name")
  lazy val mongoDbUser = conf.getString("mongo.db.user")
  lazy val mongoDbPwd = conf.getString("mongo.db.password")

  lazy val mongoClient = MongoInstance.getInstance(mongoHost, mongoPort, mongoDbName, mongoDbUser, mongoDbPwd)

  lazy val db = mongoClient(mongoDbName)
  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()
}

object MongoInstance {
  // not found better way to have mongoinstance
  var instance: Option[MongoClient] = None

  def getInstance(mongoHost: String, mongoPort: Int, mongoDbName: String, mongoDbUser: String, mongoDbPwd: String): MongoClient = {
    val pool = 1

    def open = {
      println(s"*** mongoHost: $mongoHost, mongoPort: $mongoPort")
      val options = MongoClientOptions(connectionsPerHost = 1)
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

