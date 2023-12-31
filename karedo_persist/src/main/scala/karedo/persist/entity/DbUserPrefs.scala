package karedo.persist.entity

import java.util.UUID

import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now

/*
case class UserPref
(
  @Key("code") code: String
  , value: Double
)
*/
case class UserPrefData(value: Double, name: String, order: Int, include:Boolean = true)

case class UserPrefs
(
  // it is the AccountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , prefs: Map[String, UserPrefData] = Map()
  , ts_created: Option[DateTime] = Some(now)
  , ts_updated: DateTime = now
)
extends Keyable[String]

trait DbUserPrefs extends DbMongoDAO_Casbah[String,UserPrefs]





