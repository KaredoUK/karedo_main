package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

case class UserAccount
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , info: Option[String] = None
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: DateTime = now
  , ts_updated: DateTime = now

) extends Keyable[String]

case class Mobile
(
  msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = now
  , ts_validated: Option[DateTime] = None
  , active: Boolean = false
)

case class Email
(
  address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = now
  , ts_validated: Option[DateTime] = None
)

trait DbUserAccount extends DbMongoDAO[String,UserAccount]