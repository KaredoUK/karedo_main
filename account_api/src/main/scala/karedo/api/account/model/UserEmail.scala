package karedo.api.account.model

import karedo.api.account.entity.MongoKeyableEntity
import org.joda.time.DateTime
import org.joda.time.DateTime.now
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.Macros

case class UserEmail
(
  // it is the full email address
  _id: String
  , account_id: String
  , active: Boolean = false
  , ts_created: DateTime = now
  , ts_updated: DateTime = now
) extends MongoKeyableEntity

object UserEmail {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[UserEmail] = Json.format
  implicit def handler = Macros.handler[UserEmail]
}
