package karedo.entity

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.entity.dao.Util.now


case class UserMobile
(
  // it is the msisdn mobile number!
  @Key("_id") id: String
  , account_id: String
  , active: Boolean
  , ts_created: DateTime = now
  , ts_updated: DateTime = now
)
extends Keyable[String]

trait DbUserMobile extends DbMongoDAO[String,UserMobile]





