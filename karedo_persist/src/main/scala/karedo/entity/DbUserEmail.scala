package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._


case class UserEmail
(
  // it is the full email address
  @Key("_id") id: String
  , account_id: UUID
  , active: Boolean
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_updated: DateTime = new DateTime(DateTimeZone.UTC).plusMinutes(20)
)

trait DbUserEmail extends DbMongoDAO[String,UserEmail]





