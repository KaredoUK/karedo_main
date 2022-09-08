package karedo.entity

import java.util.UUID

import com.mongodb.casbah.Imports._
import karedo.entity.dao._
import karedo.util.{KO, Result, Util}
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
  , karedos: Double = 0
  , lockedBy: String = ""
  , ts: DateTime = now
)
extends Keyable[String]

trait DbUserKaredos extends DbMongoDAO[String,UserKaredos] {
  def addKaredos(accountId: String, points: Double): Result[String,UserKaredos] = {
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
  def transferKaredo(from: String, to: String, amount: Double) = {
    // better implementation of transfer with locking
    if(from<to) transferKaredoOrdered(from,to,amount)
    else transferKaredoOrdered(to,from,-amount)
  }
  // see unit tests for effective testing this
  private def transferKaredoOrdered(from:String, to: String, amount: Double): Result[String,UserKaredos] ={
    val transid = Util.newUUID
    val result = for {

      acc1 <- lock(from, transid)
      acc2 <- lock(to, transid)
      acc1upd <- update(acc1.copy(karedos=acc1.karedos - amount, ts = now))
      acc2upd <- update(acc2.copy(karedos=acc2.karedos + amount, ts = now))
      unlock2 <- unlock(to, transid)
      unlock1 <- unlock(from, transid)

    } yield unlock1
    //println(s"changed account $result")
    result
  }
  // this is the transfer without lock which is dangerous
  def transferKaredoNaive(from:String, to: String, amount: Double): Result[String,UserKaredos] ={
    val transid = Util.newUUID
    val result = for {

      acc1 <- find(from)
      acc2 <- find(to)
      acc1upd <- update(acc1.copy(karedos=acc1.karedos - amount, ts = now))
      acc2upd <- update(acc2.copy(karedos=acc2.karedos + amount, ts = now))

    } yield acc2upd
    //println(s"changed account $result")
    result
  }
}

