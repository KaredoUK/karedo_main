package karedo.persist.entity

import java.util.UUID

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import karedo.persist.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/1/16.
  */
case class RequestMessage
(
  source: Option[String] = None
  , request: Option[String] = None
  , entity: Option[String] = None
  , headers: Option[Map[String, String]] = None
  , protocol: Option[String] = None
  , uri: Option[String] = None
  , method: Option[String] = None
  , ts: DateTime = now
)

case class ResponseMessage
(
  source: Option[String] = None
  , response: Option[String] = None
  , entity: Option[String] = None
  , headers: Option[Map[String, String]] = None
  , protocol: Option[String] = None
  , status: Option[String] = None
  , ts: DateTime = now
)

case class RTBMessage
(
  // message id
  @Key("_id") id: String = UUID.randomUUID().toString
  , request: RequestMessage
  , response: ResponseMessage
  , ts: DateTime = now
)
extends Keyable[String]

trait DbRTBMessages extends DbMongoDAO_Casbah[String,RTBMessage]

case class AdMessage
(
  // message id
  @Key("_id") id: String = UUID.randomUUID().toString
  , request: RequestMessage
  , response: ResponseMessage
  , ts: DateTime = now
)
extends Keyable[String]

trait DbAdMessages extends DbMongoDAO_Casbah[String,AdMessage]

case class APIMessage
(
  // message id
  @Key("_id") id: String = UUID.randomUUID().toString
  , request: RequestMessage
  , response: ResponseMessage
  , ts: DateTime = now
)
  extends Keyable[String]

trait DbAPIMessages extends DbMongoDAO_Casbah[String,APIMessage]
