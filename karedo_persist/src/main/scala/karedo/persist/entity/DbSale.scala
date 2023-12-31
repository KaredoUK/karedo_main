package karedo.persist.entity

import java.util.UUID

import karedo.common.misc.Util
import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now

case class Sale
(
  @Key("_id") id: String = Util.newUUID
  , receiver_id: String = ""
  , receiver_name: String = ""
  , receiver_msisdn: String = ""
  , sender_id: String = ""
  , sender_name: String = ""
  , sender_msisdn: String = ""
  , karedos: Long = 0
  , sale_type: String = ""
  , status: String = ""
  , ts_created: DateTime = now
  , ts_updated: DateTime = now
  , ts_completed: Option[DateTime] = None

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbSale extends DbMongoDAO_Casbah[String,Sale]



