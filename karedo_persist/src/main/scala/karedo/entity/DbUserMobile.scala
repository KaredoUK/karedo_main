package karedo.entity

import karedo.entity.dao.{DbDao, DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._


case class UserMobile
(
  // it is the msisdn mobile number!
  @Key("_id") id: String
  , account_id: String
  , active: Boolean
  , ts_created: DateTime = DbDao.now
  , ts_updated: DateTime = DbDao.now
)
extends Keyable[String]

trait DbUserMobile extends DbMongoDAO[String,UserMobile]





