package karedo.entity.dao

import com.mongodb.casbah.commons.MongoDBObject
import karedo.util.{KO, OK, Result, Util}
import org.slf4j.LoggerFactory
import salat._
import salat.dao.SalatDAO
import salat.global._

import scala.util.{Failure, Success, Try}


abstract class DbMongoDAO_Casbah[K, T <: Keyable[K]]
(implicit
 val manifestT: Manifest[T]
 , val manifestK: Manifest[K]
)

  extends DbDAO[K, T]
    with DbDAOExtensions[K, T]
    with MongoConnection_Casbah {


  def byId(userId: K) = MongoDBObject("_id" -> userId)

  def byField[F](fname: String, value: F) = MongoDBObject(fname -> value)

  val thisClass = manifestT.runtimeClass
  val simpleName = thisClass.getSimpleName
  val logger = LoggerFactory.getLogger(thisClass)
  //logger.debug(s"setting up $thisClass")

  lazy val dao = new SalatDAO[T, K](collection = db(s"${DbDAOParams.tablePrefix}$simpleName")) {}

  override def insertNew(r: T): Result[String, T] = {
    val id = r.id
    logger.debug(s"insertNew $r")
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
    logger.debug(s"find $id")
    val ret = Try {
      dao.findOneById(id)
    } match {
      case Success(Some(x)) => OK(x)
      case Success(None) =>
        KO(s"No record found in table ${dao.collection.name} for id = $id")
      case Failure(x) =>
        KO(x.toString)

    }
    logger.debug(s"getById returning $ret")
    ret

  }

  override def findAll(): Result[String, List[T]] = {
    logger.debug(s"find all")
    val ret = Try[List[T]] {
      val cursor = dao.find(MongoDBObject.empty)
      cursor.toList
    } match {
      case Success(r) => OK(r)
      case Failure(x) =>
        KO(x.toString)
    }
    logger.debug(s"find all returning $ret")
    ret
  }

  override def findByAccount(account_id: String): Result[String, List[T]] = {
    logger.debug(s"find by account $account_id")
    val ret = Try {
      dao.find(MongoDBObject("account_id" -> account_id) )
    } match {
      case Success(x) if x.isEmpty => KO(s"No record found in table ${dao.collection.name} for account_id = $account_id")
      case Success(x) => OK(x.toList)
      case Failure(x) =>
        KO(x.toString)
    }
    logger.debug(s"getById returning $ret")
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

  override def unlock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts") = {
    val ret = dao.update(MongoDBObject("_id" -> id, transField -> transId),
      MongoDBObject(
        "$set" -> MongoDBObject(transField -> "", tsField -> Util.now)
      ), upsert = false)
    if(ret.getN()<1) logger.error("Unlock failed (not previously locked")
    find(id)
  }

  override def ids: Result[String, List[K]] = {
    logger.debug(s"findAll")
    val ret = Try {
      dao.ids(MongoDBObject("_id" -> MongoDBObject("$exists" -> true)))
    } match {
      case Success(x) => OK(x)
      case Failure(x) =>
        KO(x.toString)

    }
    logger.debug(s"ids returning $ret")
    ret

  }

  override def update(r: T): Result[String, T] = {
    val id = r.id
    logger.debug(s"updating $r")
    Try {
      dao.update(byId(id), grater[T].asDBObject(r))
    } match {
      case Success(x) => OK(r)
      case Failure(error) => KO(error.toString)
    }
  }

  override def delete(r: T): Result[String, T] = {
    val id = r.id
    logger.debug(s"deleting id: $id}")
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

