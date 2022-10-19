package karedo.rtb.util

import com.typesafe.config.ConfigFactory

trait Configurable {

  val conf = ConfigFactory.load("exchange.conf")

}
