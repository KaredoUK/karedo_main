package karedo.entity

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._


case class UserEmail
(
  // it is the full email address
  @Key("_id") id: String
  , account_id: String
  , active: Boolean
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_updated: DateTime = new DateTime(DateTimeZone.UTC).plusMinutes(20)
) extends Keyable[String]

trait DbUserEmail extends DbMongoDAO[String,UserEmail]





