package karedo.entity

import java.util.UUID

import karedo.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

/**
  * Created by pakkio on 10/1/16.
  */
case class UserProfile
(
  // id is the same as AccountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , gender: Option[String] = None
  , first_name: Option[String] = Some("")
  , last_name: Option[String] = Some("")
  , yob: Option[Int] = None
  , kids: Option[Int] = None
  , income: Option[Int] = None
  , postcode: Option[String] = None
  , location: Option[Boolean] = Some(true)
  , opt_in: Option[Boolean] = Some(true)
  , third_party: Option[Boolean] = Some(true)
  , ts_created: DateTime = now
  , ts_updated: DateTime = now

) extends Keyable[String]

trait DbUserProfile extends DbMongoDAO_Casbah[String,UserProfile]