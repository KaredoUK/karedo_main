package karedo.persist.entity

import java.util.UUID

import karedo.common.misc.Util
import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now

case class EmailVerify
(
  @Key("_id") id: String = Util.newUUID
  , account_id: String = ""
  , application_id: String = ""
) extends Keyable[String]

// add implementation if you need special functionalities
trait DbEmailVerify extends DbMongoDAO_Casbah[String,EmailVerify]



