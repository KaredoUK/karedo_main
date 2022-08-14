package karedo.entity.dao

import com.typesafe.config.ConfigFactory

trait Configurable {

  val conf = ConfigFactory.load()

}
