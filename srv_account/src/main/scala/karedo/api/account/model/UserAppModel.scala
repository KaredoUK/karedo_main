package karedo.api.account.model

import karedo.common.misc.Util._
import karedo.common.mongo.reactive.MongoKeyableEntity
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import reactivemongo.bson.Macros

case class UserApp
(
  // this is the application id which must be univoque
  _id: String = newMD5
  , account_id: String = newUUID
  // Deprecated
  // , map_confirmed: Boolean = false
  // Only true if UserMobile.account_id = UserApp.account_id
  , mobile_linked: Boolean = false
  // Only true if UserEmail.account_id = UserApp.account_id
  , email_linked: Boolean = false
  , ts: DateTime = now
) extends MongoKeyableEntity

object UserApp {
  import karedo.common.mongo.ReactiveJodaDateImplicits._

  implicit def format: Format[UserApp] = Json.format
  implicit def handler = Macros.handler[UserApp]
}
