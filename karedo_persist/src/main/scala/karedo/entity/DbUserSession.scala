package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._


case class UserSession
(
  @Key("_id") id: UUID = UUID.randomUUID()
  , account_id: UUID
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_expire: DateTime = new DateTime(DateTimeZone.UTC).plusMinutes(20)
  , info: Option[String] = None
)

trait DbUserSession extends DbMongoDAO[UUID,UserSession]





