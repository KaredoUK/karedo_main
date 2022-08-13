package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class UserKaredos
(
  // accountId
  @Key("_id") id: UUID = UUID.randomUUID()
  , karedos: Int = 0
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)

trait DbUserKaredos extends DbMongoDAO[UUID,UserKaredos]

