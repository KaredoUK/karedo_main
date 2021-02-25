package core

import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat

trait ISODateConversion {
  implicit def dateWrapper(s: DateTime) =
    ISODateTimeFormat.dateTime().print(s.toDateTime(DateTimeZone.UTC))

}
