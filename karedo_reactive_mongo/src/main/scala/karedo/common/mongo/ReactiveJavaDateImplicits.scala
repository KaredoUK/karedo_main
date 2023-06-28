package karedo.common.mongo

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}

import org.joda.time.{DateTime, DateTimeZone}
import reactivemongo.bson.{BSONDateTime, BSONHandler}

object ReactiveJavaDateImplicits {

  implicit object JavaDateImpicits extends BSONHandler[BSONDateTime, LocalDateTime] {
    def read(time: BSONDateTime) = LocalDateTime.ofInstant(Instant.ofEpochMilli(time.value), ZoneId.systemDefault())

    def write(time: LocalDateTime) = BSONDateTime(time.toInstant(ZoneOffset.UTC).toEpochMilli())
  }

}

object ReactiveJodaDateImplicits {
  implicit object JodaDateImpicits extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = new DateTime(time.value, DateTimeZone.UTC)
    def write(time: DateTime) = BSONDateTime(time.getMillis)
  }
}