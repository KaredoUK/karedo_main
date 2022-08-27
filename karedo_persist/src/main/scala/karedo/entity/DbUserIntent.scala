package karedo.entity

import java.util.UUID

import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

/**
  * Created by crajah on 14/10/2016.
  */
case class UserIntent (
                        @Key("_id") id:String
                      , intents: List[Intent]
                      ) extends Keyable[String]

case class Intent (
                  intent_id: String = UUID.randomUUID().toString
                  , why: String
                  , what: String
                  , when: String
                  , where: String
                  , ts_updated: DateTime = now
                  )


trait DbUserIntent extends DbMongoDAO[String,UserIntent]
