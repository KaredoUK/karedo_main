package karedo.entity

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

case class UserApp
(
  // this is the application id which must be univoque
  @Key("_id") id: String
  , account_id: String
  , map_confirmed: Boolean = false
  , ts: DateTime = new DateTime(DateTimeZone.UTC)

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbUserApp extends DbMongoDAO[String,UserApp]



