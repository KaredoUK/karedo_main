package karedo.entity.dao

import java.util.UUID

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.typesafe.config.Config
import org.slf4j.LoggerFactory
import salat.dao.SalatDAO
import salat._
import salat.global._

import scala.util.Try

class DbMongoDAO[K, T <: AnyRef]
(implicit val conf: Config
 , val manifestT: Manifest[T]
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

  override def insertNew(id: K, r: T): Try[Option[K]] = {
    logger.info(s"insertNew $r")
    Try(
      dao.insert(
        r
      )
    )
  }

  override def getById(id: K) = {
    logger.info(s"getById $id")
    val dbuser = dao.findOneById(id)
    logger.info(s"getById returning $dbuser")

    dbuser
  }

  override def update(id: K, r: T) = {
    logger.info(s"updating $r")
    Try {
      dao.update(byId(id), grater[T].asDBObject(r))
    }.map(r => r.toString)
  }

  override def delete(id: K, r: T) = {
    logger.info(s"deleting id: $id}")
    Try {
      dao.removeById(id)
    }.map(r => r.toString)
  }

  override def deleteAll() = {
    logger.warn(s"deleting all from $simpleName")
    dao.collection.remove(MongoDBObject())
  }
}

