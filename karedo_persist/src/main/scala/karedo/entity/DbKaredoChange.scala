package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class KaredoChange
(
  // AccountId
  @Key("_id") id: UUID = UUID.randomUUID()
  , karedos: Int
  , trans_type: String
  , trans_info: String
  , trans_currency: String
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

trait DbKaredoChange extends DbMongoDAO[UUID,KaredoChange]
