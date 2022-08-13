package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import salat.annotations._
import org.joda.time.{DateTime, DateTimeZone}

case class UserProfile
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , gender: String
  , first_name: String
  , last_name: String
  , yob: Option[Int] = None
  , kids: Option[Int] = None
  , income: Option[Int] = None
  , location: Boolean = false
  , opt_in: Boolean = false
  , third_party: Boolean = false
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_updated: DateTime = new DateTime(DateTimeZone.UTC)

)

case class Mobile
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = DateTime.now(DateTimeZone.UTC)
  , ts_validated: Option[DateTime] = None
  , active: Boolean = false
)

case class Email
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = DateTime.now(DateTimeZone.UTC)
  , ts_validated: Option[DateTime] = None
)

case class UserApp
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , map_confirmed: Boolean = false
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

case class UserSession
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , info: String
  , ts_expire: DateTime = new DateTime(DateTimeZone.UTC).plusMinutes(20)
)

case class UserKaredos
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , karedos: Int = 0
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

case class KaredoChange
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , karedos: Int
  , trans_type: String
  , trans_info: String
  , trans_currency: String
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

case class UserAccount
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , info: Option[String] = None
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , userProfile: Option[UserProfile] = None
  , karedos: UserKaredos = UserKaredos()
  , transactions: List[KaredoChange] = List()

  , userApp: List[UserApp] = List()
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: DateTime = DateTime.now(DateTimeZone.UTC)
  , ts_updated: DateTime = DateTime.now(DateTimeZone.UTC)

  , userSession: List[UserSession] = List()
)

trait DbUserAccount extends DbMongoDAO[UserAccount]