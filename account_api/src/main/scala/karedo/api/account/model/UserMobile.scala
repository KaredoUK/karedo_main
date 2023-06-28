package karedo.api.account.model

import karedo.api.account.entity.MongoKeyableEntity
import karedo.common.misc.Util.newUUID
import org.joda.time.DateTime
import org.joda.time.DateTime.now
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.Macros

case class UserMobile
(
  // it is the msisdn mobile number!
  _id: String = newUUID
  , account_id: String
  , active: Boolean
  , ts_created: DateTime = now
  , ts_updated: DateTime = now
) extends MongoKeyableEntity

object UserMobile {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[UserMobile] = Json.format
  implicit def handler = Macros.handler[UserMobile]
}

