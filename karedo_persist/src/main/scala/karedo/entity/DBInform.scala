package karedo.entity

import java.util.UUID

import karedo.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

/**
  * Created by charaj on 25/01/2017.
  */
case class Inform
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , account_id: String
  , inform_type: String
  , subject: String
  , detail: Option[String]
  , image_base64: Option[String]
  , status: String = "CREATED"
  , jira: Option[Jira] = None
  , ts_created: Option[DateTime] = Some(now)
  , ts_updated: Option[DateTime] = Some(now)
) extends Keyable[String]

case class Jira(id: String, key: String, self: String)

trait DbInform extends DbMongoDAO_Casbah[String, Inform]
