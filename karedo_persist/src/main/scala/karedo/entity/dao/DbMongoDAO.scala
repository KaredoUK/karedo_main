package karedo.entity.dao

import com.mongodb.casbah.commons.MongoDBObject
import karedo.util.{KO, OK, Result, Util}
import org.slf4j.LoggerFactory
import salat._
import salat.dao.SalatDAO
import salat.global._

import scala.util.{Failure, Success, Try}

trait Keyable[K] {

  def id: K
}

object DbMongoDAO {
  var tablePrefix = "X"
}

abstract class DbMongoDAO[K, T <: Keyable[K]]
(implicit
 val manifestT: Manifest[T]
 , val manifestK: Manifest[K]
)

  extends DbDAO[K, T]
    with MongoConnection {


  def byId(userId: K) = MongoDBObject("_id" -> userId)

  def byField[F](fname: String, value: F) = MongoDBObject(fname -> value)

  val thisClass = manifestT.runtimeClass
  val simpleName = thisClass.getSimpleName
  val logger = LoggerFactory.getLogger(thisClass)
  //logger.info(s"setting up $thisClass")

  lazy val dao = new SalatDAO[T, K](collection = db(s"${DbMongoDAO.tablePrefix}$simpleName")) {}

  override def insertNew(r: T): Result[String, T] = {
    val id = r.id
    logger.info(s"insertNew $r")
    Try(
      dao.insert(
        r
      )
    ) match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)

    }
  }

  override def find(id: K): Result[String, T] = {
    logger.info(s"find $id")
    val ret = Try {
      dao.findOneById(id)
    } match {
      case Success(Some(x)) => OK(x)
      case Success(None) =>
        KO(s"No record found in table ${dao.collection.name} for id = $id")
      case Failure(x) =>
        KO(x.toString)

    }
    logger.info(s"getById returning $ret")
    ret

  }

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
          println("Wait too long")
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

  override def unlock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts") = {
    val ret = dao.update(MongoDBObject("_id" -> id, transField -> transId),
      MongoDBObject(
        "$set" -> MongoDBObject(transField -> "", tsField -> Util.now)
      ), upsert = false)
    if(ret.getN()<1) println("Unlock failed (not previously locked")
    find(id)
  }

  override def ids: Result[String, List[K]] = {
    logger.info(s"findAll")
    val ret = Try {
      dao.ids(MongoDBObject("_id" -> MongoDBObject("$exists" -> true)))
    } match {
      case Success(x) => OK(x)
      case Failure(x) =>
        KO(x.toString)

    }
    logger.info(s"ids returning $ret")
    ret

  }

  override def update(r: T): Result[String, T] = {
    val id = r.id
    logger.info(s"updating $r")
    Try {
      dao.update(byId(id), grater[T].asDBObject(r))
    } match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)
    }
  }

  override def delete(r: T): Result[String, T] = {
    val id = r.id
    logger.info(s"deleting id: $id}")
    Try {
      dao.removeById(id)
    } match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)
    }
  }

  override def deleteAll(): Result[String, Unit] = {
    logger.warn(s"deleting all from $simpleName")
    Try {
      dao.collection.remove(MongoDBObject())
    } match {
      case Success(x) => OK(Unit)
      case Failure(error) => KO(error.toString)
    }
  }
}

