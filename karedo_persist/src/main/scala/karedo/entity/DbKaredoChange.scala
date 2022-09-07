package karedo.entity

import java.util.UUID

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao._
import karedo.util.Util.now
import org.joda.time.DateTime
import salat.annotations._

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/1/16.
  */

/*
CREATED, SEND_RECEIVE, SALE_APP, REDEEM_PAYPAL, REDEEM_BANK, REDEEM_CARD
 */

case class KaredoChange
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , accountId: String
  , karedos: Long
  , trans_type: String
  , trans_info: String
  , trans_currency: String
  , ts: DateTime = now

) extends Keyable[String]

trait DbKaredoChange extends DbMongoDAO[String,KaredoChange] {

  def byAccount(id:String) = MongoDBObject("accountId" -> id)

  def getChanges(id:String) = {
    dao.find(byAccount(id)).sort(orderBy = MongoDBObject("ts" -> 1)).toList
  }
}
