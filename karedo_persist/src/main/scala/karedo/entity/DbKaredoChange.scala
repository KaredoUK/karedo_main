package karedo.entity

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class KaredoChange
(

  @Key("_id") id: UUID = UUID.randomUUID()
  , accountId: UUID
  , karedos: Int
  , trans_type: String
  , trans_info: String
  , trans_currency: String
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

trait DbKaredoChange extends DbMongoDAO[UUID,KaredoChange] {
  def byAccount(id:UUID) = MongoDBObject("accountId" -> id)

  def getChanges(id:UUID) = {
    dao.find(byAccount(id)).sort(orderBy = MongoDBObject("ts" -> 1)).toList
  }
}
