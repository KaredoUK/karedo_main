package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.entity.dao.Util.now

/**
  * Created by pakkio on 10/1/16.
  */
case class UserProfile
(
  // id is the same as AccountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , gender: String
  , first_name: String
  , last_name: String
  , yob: Option[Int] = None
  , kids: Option[Int] = None
  , income: Option[Int] = None
  , location: Boolean = false
  , opt_in: Boolean = false
  , third_party: Boolean = false
  , ts_created: DateTime = now
  , ts_updated: DateTime = now

) extends Keyable[String]

trait DbUserProfile extends DbMongoDAO[String,UserProfile]