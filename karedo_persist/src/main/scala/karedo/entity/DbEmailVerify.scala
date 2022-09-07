package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now
import karedo.util.Util

case class EmailVerify
(
  @Key("_id") id: String = Util.newUUID
  , account_id: String = ""
  , application_id: String = ""
) extends Keyable[String]

// add implementation if you need special functionalities
trait DbEmailVerify extends DbMongoDAO[String,EmailVerify]



