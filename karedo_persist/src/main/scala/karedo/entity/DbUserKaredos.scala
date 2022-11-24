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
  , karedos: Long = 0
  , lockedBy: String = ""
  , ts: DateTime = now
)
extends Keyable[String]

trait DbUserKaredos extends DbMongoDAO1[String,UserKaredos] {
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
  def transferKaredo(
                      pfrom: String,
                      pto: String,
                      pamount: Long,
                      transType: String,
                      text: String = "",
                      currency: String = "KAR",
                      history:DbKaredoChange = new DbKaredoChange {}) = {
    val transInfo = s"Moved Karedos: $pamount from $pfrom to $pto -> $text"

    def transferKaredoOrdered(from:String, to: String, amount: Long): Result[String,UserKaredos] = {
      val transid = Util.newUUID
      var step = 0
      val result = for {

        acc1 <- { step=1; lock(from, transid) }
        acc2 <- { step=2; lock(to, transid) }
        change1 <- { step=3; history.insertNew(
          KaredoChange(
            accountId = from,
            karedos = -amount,
            trans_type = transType,
            trans_info = transInfo,
            trans_currency = currency))
        }
        acc1upd <- { step=4; update(acc1.copy(karedos=acc1.karedos - amount, ts = now)) }
        change2 <- { step=5; history.insertNew(
          KaredoChange(
            accountId = to,
            karedos = amount,
            trans_type = transType,
            trans_info = transInfo,
            trans_currency = currency))
        }
        acc2upd <- { step=6; update(acc2.copy(karedos=acc2.karedos + amount, ts = now)) }

      } yield acc2upd

      // be sure to remove any locks
      unlock(to, transid)
      unlock(from, transid)

      //{ println(s"step $step ")}
      //println(s"changed account $result")
      result
    }
    if(pfrom<pto) transferKaredoOrdered(pfrom,pto,pamount)
    else transferKaredoOrdered(pto,pfrom,-pamount)
  }

  // this is the transfer without lock which is dangerous
  def transferKaredoNaive(from:String, to: String, amount: Long): Result[String,UserKaredos] ={
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

