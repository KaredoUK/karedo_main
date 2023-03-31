package karedo.persist.entity

import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now


case class UserEmail
(
  // it is the full email address
  @Key("_id") id: String
  , account_id: String
  , active: Boolean = false
  , ts_created: DateTime = now
  , ts_updated: DateTime = now
) extends Keyable[String]

trait DbUserEmail extends DbMongoDAO_Casbah[String,UserEmail]





