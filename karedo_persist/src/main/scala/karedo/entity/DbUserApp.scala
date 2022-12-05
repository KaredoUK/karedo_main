package karedo.entity

import karedo.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now
import karedo.util.Util

case class UserApp
(
  // this is the application id which must be univoque
  @Key("_id") id: String = Util.newMD5
  , account_id: String = Util.newUUID
  // Deprecated
  // , map_confirmed: Boolean = false
  // Only true if UserMobile.account_id = UserApp.account_id
  , mobile_linked: Boolean = false
  // Only true if UserEmail.account_id = UserApp.account_id
  , email_linked: Boolean = false
  , ts: DateTime = now

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbUserApp extends DbMongoDAO_Casbah[String,UserApp]



