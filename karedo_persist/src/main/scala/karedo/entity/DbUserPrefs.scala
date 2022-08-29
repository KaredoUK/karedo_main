package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import karedo.util.Result
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

/*
case class UserPref
(
  @Key("code") code: String
  , value: Double
)
*/

case class UserPrefs
(
  // it is the AccountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , prefs: Map[String, Double]
  , ts_created: Option[DateTime] = Some(now)
  , ts_updated: DateTime = now
)
extends Keyable[String]

trait DbUserPrefs extends DbMongoDAO[String,UserPrefs]





