package karedo.util

import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.ISODateTimeFormat
import spray.json.{JsString, JsValue, RootJsonFormat, _}

/**
  * Created by pakkio on 10/9/16.
  */
object DateTimeJsonHelper {
  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

//    val formatter = ISODateTimeFormat.basicDateTimeNoMillis
    val formatter = ISODateTimeFormat.basicDateTime

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj.withZone(DateTimeZone.UTC)))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s).withZone(DateTimeZone.UTC)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }

  }
}
