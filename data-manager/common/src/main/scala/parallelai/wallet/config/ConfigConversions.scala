package parallelai.wallet.config

import com.escalatesoft.subcut.inject.config.ConfigProperty
import scala.concurrent.duration._

object ConfigConversions {

  implicit def toDuration(prop: ConfigProperty): Duration = {
    if(prop.value == "infinite") {
      Duration.Inf
    } else {
      val (amount: Long, timeQualifier: String) =
        if(prop.value.contains('.')) {
          val tokens = prop.value.split('.')
          require(tokens.length == 2, s"Invalid format for property ${prop.name}: ${prop.value}, expected amount.timeQualifier")
          (tokens(0).toLong, tokens(1))
        } else {
          (prop.value.toLong, "millis")
        }

      timeQualifier match {
        case "seconds" | "secs" | "second" | "sec" => amount.seconds
        case "minutes" | "minute" | "min" | "mins" => amount.minutes
        case "hours"  | "hour" => amount.hours
        case _ => amount.milliseconds
      }
    }
  }

  implicit def toBoolean(prop: ConfigProperty): Boolean = prop.value.equalsIgnoreCase("true")

  implicit def toListOfString(prop: ConfigProperty): List[String] = prop.value.split(",").toList
}
