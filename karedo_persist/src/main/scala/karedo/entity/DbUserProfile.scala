package karedo.entity

import java.util.UUID

import karedo.entity.dao.DbMongoDAO
import org.joda.time.{DateTime, DateTimeZone}
import salat.annotations._

/**
  * Created by pakkio on 10/1/16.
  */
case class UserProfile
(
  // id is the same as AccountId
  @Key("_id") id: UUID = UUID.randomUUID()
  , gender: String
  , first_name: String
  , last_name: String
  , yob: Option[Int] = None
  , kids: Option[Int] = None
  , income: Option[Int] = None
  , location: Boolean = false
  , opt_in: Boolean = false
  , third_party: Boolean = false
  , ts_created: DateTime = new DateTime(DateTimeZone.UTC)
  , ts_updated: DateTime = new DateTime(DateTimeZone.UTC)

)

trait DbUserProfile extends DbMongoDAO[UUID,UserProfile]