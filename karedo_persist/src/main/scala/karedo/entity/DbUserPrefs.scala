package karedo.entity

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now


case class UserPref
(
  code: String
  , value: Double
)

case class UserPrefs
(
  // it is the AccountId
  @Key("_id") id: String
  , prefs: List[UserPref]
  , ts_created: Option[DateTime] = Some(now)
  , ts_updated: DateTime = now
)
extends Keyable[String]

trait DbUserPrefs extends DbMongoDAO[String,UserPrefs]





