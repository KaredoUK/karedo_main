package karedo.entity.dao

import com.mongodb.casbah.commons.conversions.scala.{RegisterConversionHelpers, RegisterJodaTimeConversionHelpers}
import karedo.util.{Configurable, Util}

/**
  * Created by charaj on 12/01/2017.
  */
trait MongoConnectionConfig extends Configurable {
  Util.isMongoActive

  lazy val mongoHost = conf.getString("mongo.server.host")
  lazy val mongoPort = conf.getInt("mongo.server.port")
  lazy val mongoDbName = conf.getString("mongo.db.name")
  lazy val mongoDbUser = conf.getString("mongo.db.user")
  lazy val mongoDbPwd = conf.getString("mongo.db.password")

  lazy val mongoURL = "mongodb://admin:FNPQYDEFFCQDCGWX@sl-eu-lon-2-portal.2.dblayer.com:16096,sl-eu-lon-2-portal.3.dblayer.com:16096/admin?ssl=true"

  lazy val mongoRetryCount = 5

}

