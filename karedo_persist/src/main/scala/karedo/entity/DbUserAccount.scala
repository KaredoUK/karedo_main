package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import salat.annotations._
import org.joda.time.{DateTime, DateTimeZone}

case class UserAccount
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , info: Option[String] = None
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: DateTime = DateTime.now(DateTimeZone.UTC)
  , ts_updated: DateTime = DateTime.now(DateTimeZone.UTC)
)

case class Mobile
(
  msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = DateTime.now(DateTimeZone.UTC)
  , ts_validated: Option[DateTime] = None
  , active: Boolean = false
)

case class Email
(
  address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = DateTime.now(DateTimeZone.UTC)
  , ts_validated: Option[DateTime] = None
)

trait DbUserAccount extends DbMongoDAO[UUID,UserAccount]