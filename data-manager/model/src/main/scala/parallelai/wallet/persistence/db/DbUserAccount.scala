package parallelai.wallet.persistence.db

import java.util.UUID

import com.novus.salat.annotations._
import org.joda.time.DateTime


case class Mobile
(
  msisdn: String,
  sms_code: Option[String] = None,
  valid: Boolean = false,
  ts_created: DateTime = DateTime.now(),
  ts_validated: Option[DateTime] = None,
  active: Boolean = false
)

case class Email
(
  address: String,
  email_code: Option[String] = None,
  valid: Boolean = false,
  ts_created: DateTime = DateTime.now(),
  ts_validated: Option[DateTime] = None
)

case class UserAccount
(
  @Key("_id") id: UUID = UUID.randomUUID(),
  password: Option[String] = None,
  userType: String = "CUSTOMER",

  mobile: List[Mobile] = List(),
  email: List[Email] = List(),
  temp: Boolean = true,
  ts_created: DateTime = DateTime.now()
)

trait DbUserAccount extends DbMongoDAO[UserAccount]