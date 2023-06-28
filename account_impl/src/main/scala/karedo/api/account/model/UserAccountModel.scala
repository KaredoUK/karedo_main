package karedo.api.account.model

import java.time.LocalDateTime.now
import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import java.util.UUID

import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDateTime, BSONDocumentReader, BSONDocumentWriter, BSONHandler, Macros}

object AdditionaImplicits {
  implicit object JavaDateImpicits extends BSONHandler[BSONDateTime, LocalDateTime] {
    def read(time: BSONDateTime) = LocalDateTime.ofInstant(Instant.ofEpochMilli(time.value), ZoneId.systemDefault())
    def write(time: LocalDateTime) = BSONDateTime(time.toInstant(ZoneOffset.UTC).toEpochMilli())
  }

  implicit object JodaDateImpicits extends BSONHandler[BSONDateTime, DateTime] {
    def read(time: BSONDateTime) = DateTime.ofInstant(Instant.ofEpochMilli(time.value), ZoneId.systemDefault())
    def write(time: LocalDateTime) = BSONDateTime(time.toInstant(ZoneOffset.UTC).toEpochMilli())
  }
}


case class UserAccount
(
  id: String = UUID.randomUUID().toString
  , info: Option[String] = None
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: LocalDateTime = now
  , ts_updated: LocalDateTime = now
)

object UserAccount {
  import AdditionaImplicits._

  implicit def format: Format[UserAccount] = Json.format
  implicit def handler = Macros.handler[UserAccount]
}

case class Mobile
(
  msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: LocalDateTime = now
  , ts_validated: Option[LocalDateTime] = None
  // Chandan - took this out, beacuse not sure how to use it yet.
  //  , active: Boolean = false
)

object Mobile {
  import AdditionaImplicits._

  implicit def format: Format[Mobile] = Json.format
  implicit def handler = Macros.handler[Mobile]
}

case class Email
(
  address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: LocalDateTime = now
  , ts_validated: Option[LocalDateTime] = None
)

object Email {
  import AdditionaImplicits._

  implicit def format: Format[Email] = Json.format
  implicit def handler = Macros.handler[Email]
}

