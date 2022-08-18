package karedo.entity

import karedo.entity.dao.{DbDao, DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._


case class UserEmail
(
  // it is the full email address
  @Key("_id") id: String
  , account_id: String
  , active: Boolean
  , ts_created: DateTime = DbDao.now
  , ts_updated: DateTime = DbDao.now.plusMinutes(20)
) extends Keyable[String]

trait DbUserEmail extends DbMongoDAO[String,UserEmail]





