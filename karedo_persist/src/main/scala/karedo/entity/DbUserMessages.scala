package karedo.entity

import java.util.UUID

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/1/16.
  */
case class Action
(
  action_type: Option[String] = None
  , action_link: Option[String] = None
)
case class UserMessages
(
  // message id
  @Key("_id") id: String = UUID.randomUUID().toString
  , account_id: String
  , tweet_text: Option[String] = None
  , short_text: Option[String] = None
  , long_text: Option[String] = None
  , action: Action = Action()
)
extends Keyable[String]

trait DbUserMessages extends DbMongoDAO[String,UserMessages] {
  def getMessages(accountId: String) =
    dao.find(MongoDBObject("account_id" -> accountId)).toList

}

