package karedo.api.account.model

import java.util.UUID

import karedo.api.account.entity.MongoKeyableEntity
import karedo.common.misc.Util.now
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.Macros

case class UserProfile
(
  // id is the same as AccountId
  _id: String = UUID.randomUUID().toString
  , gender: Option[String] = None
  , first_name: Option[String] = Some("")
  , last_name: Option[String] = Some("")
  , yob: Option[Int] = None
  , kids: Option[Int] = None
  , income: Option[Int] = None
  , postcode: Option[String] = None
  , location: Option[Boolean] = Some(true)
  , opt_in: Option[Boolean] = Some(true)
  , third_party: Option[Boolean] = Some(true)
  , ts_created: DateTime = now
  , ts_updated: DateTime = now

) extends MongoKeyableEntity

object UserProfile {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[UserProfile] = Json.format
  implicit def handler = Macros.handler[UserProfile]
}
