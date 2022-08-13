package karedo.persistence.mongodb

import com.typesafe.config.{Config, ConfigFactory}

trait MongoTestUtils {
  implicit val conf:Config = ConfigFactory.load()


}
