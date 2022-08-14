package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

case class UserApp
(
  // this is the application id which must be univoque
  @Key("_id") id: String
  , account_id: UUID
  , map_confirmed: Boolean = false
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

// add implementation if you need special functionalities
trait DbUserApp extends DbMongoDAO[String,UserApp]



