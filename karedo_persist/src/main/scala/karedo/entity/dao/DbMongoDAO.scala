package karedo.entity.dao

import java.util.UUID

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import salat.dao.SalatDAO
import salat._
import salat.global._

import scala.util.{Failure, Success, Try}

class DbMongoDAO[K, T <: AnyRef]
(implicit
   val manifestT: Manifest[T]
 , val manifestK: Manifest[K]
)

  extends DbDAO[K, T]
    with MongoConnection {

  def byId(userId: K) = MongoDBObject("_id" -> userId)

  val thisClass = manifestT.runtimeClass
  val simpleName = thisClass.getSimpleName
  val logger = LoggerFactory.getLogger(thisClass)
  logger.info(s"setting up $thisClass")

  val dao = new SalatDAO[T, K](collection = db(s"X$simpleName")) {}

  override def insertNew(id: K, r: T): Result[String,Unit] = {
    logger.info(s"insertNew $r")
    Try(
      dao.insert(
        r
      )
    ) match {
      case Success(x) => OK(Unit)
      case Failure(error) => KO(error.toString)

    }
  }

  override def getById(id: K) : Result[String,T] = {
    logger.info(s"getById $id")
    val ret = Try {
      dao.findOneById(id)
    } match {
      case Success(Some(x)) => OK(x)
      case Success(None) => KO("No record found")
      case Failure(x) => KO(x.toString)

    }
    logger.info(s"getById returning $ret")
    ret

  }

  override def update(id: K, r: T): Result[String,Unit] = {
    logger.info(s"updating $r")
    Try {
      dao.update(byId(id), grater[T].asDBObject(r))
    } match {
      case Success(x) => OK(Unit)
      case Failure(error) => KO(error.toString)
    }
  }

  override def delete(id: K, r: T): Result[String,Unit] = {
    logger.info(s"deleting id: $id}")
    Try {
      dao.removeById(id)
    } match {
      case Success(x) => OK(Unit)
      case Failure(error) => KO(error.toString)
    }
  }

  override def deleteAll(): Result[String,Unit] = {
    logger.warn(s"deleting all from $simpleName")
    Try {
      dao.collection.remove(MongoDBObject())
    } match {
      case Success(x) => OK(Unit)
      case Failure(error) => KO(error.toString)
    }
  }
}

