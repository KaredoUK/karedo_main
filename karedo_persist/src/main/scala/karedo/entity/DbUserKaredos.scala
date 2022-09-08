package karedo.entity

import java.util.UUID

import com.mongodb.casbah.Imports._
import karedo.entity.dao._
import karedo.util.{KO, OK, Result}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/1/16.
  */
case class UserKaredos
(
  // accountId
  @Key("_id") id: String = UUID.randomUUID().toString
  , karedos: Long = 0
  , ts: DateTime = now
)
extends Keyable[String]

trait DbUserKaredos extends DbMongoDAO[String,UserKaredos] {
  def addKaredos(accountId: String, points: Long): Result[String,UserKaredos] = {
    Try {
      dao.findOneById(accountId) match {
        case None => insertNew(UserKaredos(accountId, points))
        case Some(r) => {
          dao.update(byId(accountId), $inc("karedos" -> points))
          find(accountId)
        }
      }
    } match {
      case Success(x) => x
      case Failure(error) => KO(error.toString)

    }
  }

  // see unit tests for effective testing this
  def transferKaredo(from:String, to: String, amount: Long): Result[String,String] ={

    val from_acc = find(from).get
    val to_acc = find(to).get

    if(from_acc.karedos < amount) KO("Not enough Karedos")

    update(from_acc.copy(karedos=from_acc.karedos - amount, ts = now))
    update(to_acc.copy(karedos=to_acc.karedos + amount, ts = now))

    OK("Success")
  }
}

