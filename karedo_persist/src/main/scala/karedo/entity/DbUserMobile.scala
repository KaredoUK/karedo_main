package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._


case class UserMobile
(
  // it is the msisdn mobile number!
  @Key("_id") id: String
  , account_id: String
  , active: Boolean
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_updated: DateTime = new DateTime(DateTimeZone.UTC).plusMinutes(20)
)
extends Keyable[String]

trait DbUserMobile extends DbMongoDAO[String,UserMobile]





