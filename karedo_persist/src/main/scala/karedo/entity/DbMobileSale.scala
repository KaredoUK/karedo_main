package karedo.entity

import java.util.UUID

import karedo.entity.dao._
import karedo.util.Util
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

/**
  * Created by crajah on 24/10/2016.
  */

case class MobileSale
(
  @Key("_id") id: String = Util.newUUID
  , sale_ids: List[String] = List()
) extends Keyable[String]

trait DbMobileSale extends DbMongoDAO_Casbah[String,MobileSale]
