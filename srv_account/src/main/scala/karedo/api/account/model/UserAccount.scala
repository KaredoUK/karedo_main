package karedo.api.account.model

import java.util.UUID

import karedo.common.mongo.reactive.MongoKeyableEntity
import org.joda.time.DateTime
import org.joda.time.DateTime.now
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.{BSONDocument, BSONHandler, Macros}

case class UserAccount
(
  _id: String = UUID.randomUUID().toString
  , info: Option[String] = None
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: DateTime = now
  , ts_updated: DateTime = now

) extends MongoKeyableEntity

object UserAccount {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[UserAccount] = Json.format
  implicit def handler = Macros.handler[UserAccount]
  implicit val bsonMobile: BSONHandler[BSONDocument, Mobile] = Macros.handler[Mobile]
  implicit val bsonEmail: BSONHandler[BSONDocument, Email] = Macros.handler[Email]
}

case class Mobile
(
  msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = now
  , ts_validated: Option[DateTime] = None
  // Chandan - took this out, beacuse not sure how to use it yet.
  //  , active: Boolean = false
)

object Mobile {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[Mobile] = Json.format
}

case class Email
(
  address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = now
  , ts_validated: Option[DateTime] = None
)

object Email {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[Email] = Json.format
}
