package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._


case class UserPref
(
  code: String
  , value: Int
)

case class UserPrefs
(
  // it is the AccountId
  @Key("_id") id: String
  , prefs: List[UserPref]
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_updated: DateTime = new DateTime(DateTimeZone.UTC).plusMinutes(20)
)
extends Keyable[String]

trait DbUserPrefs extends DbMongoDAO[String,UserPrefs]





