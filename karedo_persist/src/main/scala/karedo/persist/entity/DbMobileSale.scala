package karedo.persist.entity

import java.util.UUID

import karedo.common.misc.Util
import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._

/**
  * Created by crajah on 24/10/2016.
  */

case class MobileSale
(
  @Key("_id") id: String = Util.newUUID
  , sale_ids: List[String] = List()
) extends Keyable[String]

trait DbMobileSale extends DbMongoDAO_Casbah[String,MobileSale]
