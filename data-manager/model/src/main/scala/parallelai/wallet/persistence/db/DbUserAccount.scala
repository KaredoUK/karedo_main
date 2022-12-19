package parallelai.wallet.persistence.db

import java.util.UUID

import com.novus.salat.annotations._
import org.joda.time.DateTime

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
  , ts_created: DateTime = new DateTime()
  , ts_updated: DateTime = new DateTime()

)

case class Mobile
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = DateTime.now()
  , ts_validated: Option[DateTime] = None
  , active: Boolean = false
)

case class Email
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = DateTime.now()
  , ts_validated: Option[DateTime] = None
)

case class UserApp
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , map_confirmed: Boolean = false
  , ts: DateTime = new DateTime()
)

case class UserSession
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , ts_created: DateTime = new DateTime()
  , info: String
  , ts_expire: DateTime = new DateTime().plusMinutes(20)
)

case class UserKaredos
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , karedos: Int = 0
  , ts: DateTime = new DateTime()
)

case class KaredoChange
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , karedos: Int
  , trans_type: String
  , trans_info: String
  , trans_currency: String
  , ts: DateTime = new DateTime()
)

case class UserAccount
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , userProfile: Option[UserProfile] = None
  , karedos: UserKaredos = UserKaredos()
  , transactions: List[KaredoChange] = List()

  , userApp: List[UserApp] = List()
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: DateTime = DateTime.now()

  , userSession: List[UserSession] = List()
)

trait DbUserAccount extends DbMongoDAO[UserAccount]