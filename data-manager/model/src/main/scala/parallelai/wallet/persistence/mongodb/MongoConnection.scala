package parallelai.wallet.persistence.mongodb

import com.mongodb.casbah.{ MongoCredential, MongoClient }
import com.mongodb.ServerAddress
import com.escalatesoft.subcut.inject.Injectable

/**
 * Created by crajah on 06/07/2014.
 */
trait MongoConnection {
  self: Injectable =>

  lazy val mongoHost: String = injectProperty[String]("mongo.server.host")
  lazy val mongoPort: Int = injectProperty[Int]("mongo.server.port")
  lazy val mongoDbName: String = injectProperty[String]("mongo.db.name")
  lazy val mongoDbUser: String = injectProperty[String]("mongo.db.user")
  lazy val mongoDbPwd: String = injectProperty[String]("mongo.db.pwd")
  lazy val mongoClient = MongoInstance.getInstance(mongoHost,mongoPort,mongoDbName,mongoDbUser,mongoDbPwd)

  lazy val db = mongoClient(mongoDbName)

  

}
object MongoInstance {
    var instance: Option[MongoClient] = None

    def getInstance(mongoHost: String, mongoPort: Int, mongoDbName: String, mongoDbUser: String, mongoDbPwd: String): MongoClient = {
      def open = {
        println(s"*** mongoHost: $mongoHost, mongoPort: $mongoPort")
        if (mongoDbUser.isEmpty) {
          MongoClient(mongoHost, mongoPort)
        } else {
          MongoClient(new ServerAddress(mongoHost, mongoPort),
            List(MongoCredential.createMongoCRCredential(mongoDbUser, mongoDbName, mongoDbPwd.toCharArray)))
        }
      }
      if (instance == None) instance = Some(open)
      instance.get
    }

  }

