package karedo.persist.entity

import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now


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

trait DbUserMobile extends DbMongoDAO_Casbah[String,UserMobile]





