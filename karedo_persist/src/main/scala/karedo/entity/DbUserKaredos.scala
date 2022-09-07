package karedo.entity

import java.util.UUID

import com.mongodb.casbah.Imports._
import karedo.entity.dao._
import karedo.util.{KO, Result}
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
    Try[Result[String,UserKaredos]] {
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

  def moveKaredosBetweenAccounts(from: String, to: String, karedos: String): Result[String, String] = {
    Try[Result[String, String]] {
      KO("Not Yet Implemented")
    } match {
      case Success(s) => s
      case Failure(f) => KO(f.getMessage + "\n" + f.getStackTrace)
    }
  }
}

