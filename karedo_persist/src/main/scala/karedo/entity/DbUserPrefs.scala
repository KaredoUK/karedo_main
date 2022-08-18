package karedo.entity

import karedo.entity.dao.{DbDao, DbMongoDAO, Keyable}
import org.joda.time.DateTime
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
  , ts_created: DateTime = DbDao.now
  , ts_updated: DateTime = DbDao.now
)
extends Keyable[String]

trait DbUserPrefs extends DbMongoDAO[String,UserPrefs]





