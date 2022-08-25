package karedo.util

import com.typesafe.config.ConfigFactory

trait Configurable {

  val conf = ConfigFactory.load()

}
