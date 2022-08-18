package karedo.entity

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao.{DbDao, DbMongoDAO, Keyable}
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class KaredoChange
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , accountId: String
  , karedos: Int
  , trans_type: String
  , trans_info: String
  , trans_currency: String
  , ts: DateTime = DbDao.now

) extends Keyable[String]

trait DbKaredoChange extends DbMongoDAO[String,KaredoChange] {
  def byAccount(id:String) = MongoDBObject("accountId" -> id)

  def getChanges(id:String) = {
    dao.find(byAccount(id)).sort(orderBy = MongoDBObject("ts" -> 1)).toList
  }
}
