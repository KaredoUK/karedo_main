package karedo.entity.dao

import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}
import karedo.util.{Configurable, Util}

/**
  * Created by charaj on 12/01/2017.
  */
trait MongoConnectionConfig extends Configurable {
//  Util.isMongoActive

//  lazy val mongoHost = conf.getString("mongo.server.host")
//  lazy val mongoPort = conf.getInt("mongo.server.port")
//  lazy val mongoDbName = conf.getString("mongo.db.name")
//  lazy val mongoDbUser = conf.getString("mongo.db.user")
//  lazy val mongoDbPwd = conf.getString("mongo.db.password")

  lazy val mongoDbName = conf.getString("mongo.db.name")
  lazy val mongoURI = conf.getString("mongo.auth.uri")
  lazy val mongoCACertB64 = conf.getString("mongo.auth.ca_certificate_base64")

  lazy val mongoRetryCount = 5
}

