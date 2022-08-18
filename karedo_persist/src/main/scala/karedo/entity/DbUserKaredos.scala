package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class UserKaredos
(
  // accountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , karedos: Int = 0
  , ts: DateTime = new DateTime(DateTimeZone.UTC)
)
extends Keyable[String]

trait DbUserKaredos extends DbMongoDAO[String,UserKaredos]

