package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbDao, DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class UserKaredos
(
  // accountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , karedos: Int = 0
  , ts: DateTime = DbDao.now
)
extends Keyable[String]

trait DbUserKaredos extends DbMongoDAO[String,UserKaredos]

