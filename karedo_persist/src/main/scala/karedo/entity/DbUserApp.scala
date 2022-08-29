package karedo.entity

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

case class UserApp
(
  // this is the application id which must be univoque
  @Key("_id") id: String
  , account_id: String
  // Only true is Mobile mapped to Application
  , map_confirmed: Boolean = false
  , ts: DateTime = now

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbUserApp extends DbMongoDAO[String,UserApp]



