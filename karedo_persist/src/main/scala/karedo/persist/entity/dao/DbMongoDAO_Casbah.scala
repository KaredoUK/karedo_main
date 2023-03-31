package karedo.persist.entity.dao

import com.mongodb.WriteResult
import com.mongodb.casbah.commons.MongoDBObject
import karedo.route.util.{KO, OK, Result}
import org.slf4j.LoggerFactory
import salat._
import salat.dao.SalatDAO
import salat.global._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


abstract class DbMongoDAO_Casbah[K, T <: Keyable[K]] (implicit override val manifestT: Manifest[T], override val manifestK: Manifest[K])
  extends ParentDAO[K, T]
  with DbDAO[K, T]
{

  def byId(userId: K) = MongoDBObject("_id" -> userId)

  def byField[F](fname: String, value: F) = MongoDBObject(fname -> value)

//  @deprecated("use insertNew_f instead", "2017-04-12")
  override def insertNew(r: T): Result[String, T] = {
    val id = r.id
    logger.debug(s"insertNew $r")
    insertHelper(r) match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)

    }
  }

  override def insert_f(r: T): F_ID = Future.fromTry(insertHelper(r))

  private def insertHelper(r: T): Try[K] = {
      dao.insert(r).fold[Try[K]](Failure[K](new Error(s"FAILED: Insert in ${dao.collection.name} for ${r}")))(Success(_))
  }

//  @deprecated("use find_f instead", "2017-04-12")
  override def find(id: K): Result[String, T] = {
    logger.debug(s"find $id")
    val ret = findHelper(id) match {
      case Success(x) => OK(x)
      case Failure(x) =>
        KO(s"No record found in table ${dao.collection.name} for id = $id")

    }
    logger.debug(s"getById returning $ret")
    ret

  }

  override def find_f(id: K): F_TYPE = Future.fromTry(findHelper(id))

  private def findHelper(id: K): Try[T] = {
    dao.findOneById(id).fold[Try[T]](Failure[T](new Error(s"FAILED: Find in ${dao.collection.name} for ${id}")))(Success(_))
  }

//  @deprecated("use findAll_f instead", "2017-04-12")
  override def findAll(): Result[String, List[T]] = {
    logger.debug(s"find all")
    val ret = findAllHelper() match {
      case Success(r) => OK(r)
      case Failure(x) =>
        KO(x.toString)
    }
    logger.debug(s"find all returning $ret")
    ret
  }

  override def findAll_f(): F_L_TYPE = Future.fromTry(findAllHelper)

  private def findAllHelper(): Try[List[T]] = {
    Try(dao.find(MongoDBObject.empty).toList)
  }

//  @deprecated("use findByField_f instead", "2017-04-12")
  override def findByAccount(value: String, field: String = "account_id"): Result[String, List[T]] = {
    logger.debug(s"find by account $value")
    val ret = findByFieldHelper(value, field) match {
      case Success(x) => OK(x)
      case Failure(x) => KO(x.toString)
    }
    logger.debug(s"getById returning $ret")
    ret

  }

  override def findByField_f(value: String, field: String = "account_id"): F_L_TYPE = Future.fromTry(findByFieldHelper(value, field))

  private def findByFieldHelper(value: String, field: String): Try[List[T]] = {
    dao.find(MongoDBObject(field -> value)).toList match {
      case h::t => Success(h::t)
      case Nil => Failure(new Error(s"FAILED: Find All in ${dao.collection.name} for field ${field} and value ${value}"))
    }
  }

  /*
  override def lock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts", max: Int = 100): Result[String, T] = {

    val found = find(id)
    if (found.isKO) found
    else {
      //val r = dao.findAndModify();
      val ret = dao.update(MongoDBObject("_id" -> id, transField -> ""),
        MongoDBObject(
          "$set" -> MongoDBObject(transField -> transId, tsField -> Util.now)
        ), upsert = false)
      //      val ret = dao.collection.findAndModify(
      //        query = MongoDBObject("_id" -> id, transField -> ""),
      //        update = MongoDBObject(
      //          "$set" -> MongoDBObject(transField -> transId, tsField -> Util.now)
      //        ), returnNew = true, upsert = false, fields = null,
      //        sort = null,
      //        remove = false)

      if (ret.getN == 0) {
        if (max == 0) {
          logger.error("Wait too long")
          KO("Wait too long for lock to be aquired")
        } else {
          Thread.sleep(10)
          lock(id, transId, transField, tsField, max - 1)
        }
      } else {
        find(id)

      }
    }
  }
  */

  val lockField: String = "__locked_by__"
  val lockTsField: String = "__lock_ts__"


//  @deprecated("use lock_f instead", "2017-04-12")
  override def lock(id: K, lockId: String, retries: Int = 100): R_TYPE = {
    lockHelper(id, lockId, retries) match {
      case Success(s) => OK(s)
      case Failure(f) => KO(f.toString)
    }
  }

  override def lock_f(id: K, lockId: String, retries: Int = 100): F_TYPE = Future.fromTry(lockHelper(id, lockId, retries))

  private def lockHelper(id: K, lockId: String, retries: Int = 100): Try[T] = {
    val ret = for {
      t <- findHelper(id)
      r <- { dao.update(
            MongoDBObject("_id" -> t.id, lockField -> MongoDBObject( "$exists" -> "false" )),
            MongoDBObject("$set" -> MongoDBObject(lockField -> lockId, "$currentDate" -> MongoDBObject(lockTsField -> "true"))),
        upsert = false)match {
        case wr : WriteResult => {
          if( wr.wasAcknowledged() ) {
            if( wr.getN > 0 ) {
              Success(Unit)
            } else {
              Failure(new Error(s"FAILED: Lock in ${dao.collection.name} for ${id}"))
            }
          } else {
            Failure(new Error(s"FAILED: Lock in ${dao.collection.name} for ${id}"))
          }
        }
        case _ => Failure(new Error(s"Lock: Update in ${dao.collection.name} for ${id}"))
      }
      }
    } yield t

    ret match {
      case s @ Success(_) => s
      case f @ Failure(_) => {
        if( retries > 0 ) {
          Thread.sleep(10)
          lockHelper(id, lockId, retries - 1)
        } else {
          f
        }
      }
    }
  }

//  @deprecated("use unlock_f instead", "2017-04-12")
  override def unlock(id: K, lockId: String): R_TYPE = {
    unlockHelper(id, lockId) match {
      case Success(s) => OK(s)
      case Failure(f) => KO(f.toString)
    }
  }

  override def unlock_f(id: K, lockId: String): F_TYPE = Future.fromTry(unlockHelper(id, lockId))

  private def unlockHelper(id: K, lockId: String): Try[T] = {
    for {
      t <- findHelper(id)
      r <- { dao.update(
        MongoDBObject("_id" -> t.id, lockField -> lockId),
        MongoDBObject("$unset" -> MongoDBObject(lockField -> "", lockTsField -> "")),
        upsert = false)match {
        case wr : WriteResult => {
          if( wr.wasAcknowledged() ) {
            if( wr.getN > 0 ) {
              Success(Unit)
            } else {
              Failure(new Error(s"FAILED: Unlock in ${dao.collection.name} for ${id}"))
            }
          } else {
            Failure(new Error(s"FAILED: Unlock in ${dao.collection.name} for ${id}"))
          }
        }
        case _ => Failure(new Error(s"Unlock: Update in ${dao.collection.name} for ${id}"))
      }
      }
    } yield t
  }

  /*
  override def unlock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts") = {
    val ret = dao.update(MongoDBObject("_id" -> id, transField -> transId),
      MongoDBObject(
        "$set" -> MongoDBObject(transField -> "", tsField -> Util.now)
      ), upsert = false)
    if(ret.getN()<1) logger.error("Unlock failed (not previously locked")
    find(id)
  }
  */

//  @deprecated("use ids_f instead", "2017-04-12")
  override def ids: Result[String, List[K]] = {
    logger.debug(s"findAll")
    val ret = idsHelper match {
      case Success(x) => OK(x)
      case Failure(x) =>
        KO(x.toString)

    }
    logger.debug(s"ids returning $ret")
    ret

  }

  override def ids_f: F_L_ID = Future.fromTry(idsHelper)

  private def idsHelper: Try[List[K]] = {
    dao.ids(MongoDBObject("_id" -> MongoDBObject("$exists" -> true))) match {
      case h::t => Success(h::t)
      case Nil => Failure(new Error(s"FAILED: Find IDs in ${dao.collection.name}"))
    }
  }

//  @deprecated("use update_f or upsert_f instead", "2017-04-12")
  override def update(r: T): Result[String, T] = {
    val id = r.id
    logger.debug(s"updating $r")
    updateHelper(r, false) match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)
    }
  }

  override def update_f(r: T): F_UNIT = Future.fromTry(updateHelper(r, false))
  override def upsert_f(r: T): F_UNIT = Future.fromTry(updateHelper(r, true))

  private def updateHelper(r: T, upsert: Boolean): Try[Unit] = {
    dao.update(byId(r.id), grater[T].asDBObject(r), upsert) match {
      case wr : WriteResult => {
        if( wr.wasAcknowledged() ) {
          if( wr.getN > 0 ) {
            Success(Unit)
          } else {
            Failure(new Error(s"FAILED: Update in ${dao.collection.name} for ${r}"))
          }
        } else {
          Failure(new Error(s"FAILED: Update in ${dao.collection.name} for ${r}"))
        }
      }
      case _ => Failure(new Error(s"FAILED: Update in ${dao.collection.name} for ${r}"))
    }

//    dao.update(byId(r.id), grater[T].asDBObject(r), upsert).wasAcknowledged() match {
//      case true => Success(Unit)
//      case _ => Failure(new Error(s"FAILED: Update in ${dao.collection.name} for ${r}"))
//    }
  }

//  @deprecated("use delete_f instead", "2017-04-12")
  override def delete(r: T): Result[String, T] = {
    val id = r.id
    logger.debug(s"deleting id: $id}")
    deleteHelper(r) match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)
    }
  }

  override def delete_f(r: T): F_UNIT = Future.fromTry(deleteHelper(r))

  private def deleteHelper(r: T): Try[Unit] = {
    dao.removeById(r.id) match {
      case wr : WriteResult => {
        if( wr.wasAcknowledged() ) {
          if( wr.getN > 0 ) {
            Success(Unit)
          } else {
            Failure(new Error(s"FAILED: Delete in ${dao.collection.name} for ${r}"))
          }
        } else {
          Failure(new Error(s"FAILED: Delete in ${dao.collection.name} for ${r}"))
        }
      }
      case _ => Failure(new Error(s"FAILED: Delete in ${dao.collection.name} for ${r}"))
    }
  }

//  @deprecated("use deleteAll_f instead", "2017-04-12")
  override def deleteAll(): Result[String, Unit] = {
    logger.warn(s"deleting all from $simpleName")
    Try {
      dao.collection.remove(MongoDBObject())
    } match {
      case Success(x) => OK(Unit)
      case Failure(error) => KO(error.toString)
    }
  }

  override def deleteAll_f: F_UNIT = Future.fromTry(deleteAllHelper)

  private def deleteAllHelper: Try[Unit] = {
    dao.collection.remove(MongoDBObject()) match {
      case wr : WriteResult => {
        if( wr.wasAcknowledged() ) {
          if( wr.getN > 0 ) {
            Success(Unit)
          } else {
            Failure(new Error(s"FAILED: Delete All in ${dao.collection.name}"))
          }
        } else {
          Failure(new Error(s"FAILED: Delete All in ${dao.collection.name}"))
        }
      }
      case _ => Failure(new Error(s"FAILED: Delete All in ${dao.collection.name}"))
    }
  }
}

