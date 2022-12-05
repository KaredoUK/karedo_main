package karedo.entity

import karedo.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now


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





