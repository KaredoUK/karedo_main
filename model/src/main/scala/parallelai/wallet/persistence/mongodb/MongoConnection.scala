package parallelai.wallet.persistence.mongodb

import com.mongodb.casbah.{MongoCredential, MongoClient}
import com.mongodb.ServerAddress
import com.escalatesoft.subcut.inject.Injectable

/**
 * Created by crajah on 06/07/2014.
 */
trait MongoConnection  {
  self: Injectable =>

  lazy val mongoClient =
    if(mongoDbUser.isEmpty) {
      MongoClient(mongoHost, mongoPort)
    } else {
      MongoClient(new ServerAddress(mongoHost, mongoPort), List(MongoCredential.createMongoCRCredential(mongoDbUser, mongoDbName, mongoDbPwd.toCharArray)))
    }

  lazy val db = mongoClient(mongoDbName)

  lazy val mongoHost: String = injectProperty[String]("mongo.server.host")
  lazy val mongoPort: Int = injectProperty[Int]("mongo.server.port")
  lazy val mongoDbName: String = injectProperty[String]("mongo.db.name")
  lazy val mongoDbUser: String = injectProperty[String]("mongo.db.user")
  lazy val mongoDbPwd: String = injectProperty[String]("mongo.db.pwd")

}
