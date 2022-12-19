package parallelai.wallet.persistence.db

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import org.slf4j.LoggerFactory
import parallelai.wallet.persistence.mongodb.MongoConnection

import scala.util.Try

class DbMongoDAO[T<:AnyRef](implicit
                    val bindingModule: BindingModule
                    , val manifest: Manifest[T]
                   )

  extends DbDAO[T]
    with MongoConnection
    with Injectable {

  def byId(userId: UUID) = MongoDBObject("_id" -> userId)

  val thisClass = manifest.runtimeClass
  val simpleName = thisClass.getSimpleName
  val logger = LoggerFactory.getLogger(thisClass)
  logger.info(s"setting up $thisClass")

  val dao = new SalatDAO[T, UUID](collection = db(s"X$simpleName")) {}

  override def insertNew(id: UUID, r: T): Try[Option[UUID]] = {
    logger.info(s"insertNew $r")
    Try(
      dao.insert(
        r
      )
    )
  }

  override def getById(id: UUID) = {
    logger.info(s"getById $id")
    val dbuser = dao.findOneById(id)
    logger.info(s"getById returning $dbuser")

    dbuser
  }

  override def update(id: UUID, r: T): Try[WriteResult] = {
    logger.info(s"updating $r")
    Try {
      dao.update(byId(id), grater[T].asDBObject(r))
    }
  }

  override def delete(id: UUID, r: T) = {
    logger.info(s"deleting id: $id}")
    Try {
      dao.removeById(id)
    }
  }
  override def deleteAll() = {
    logger.warn(s"deleting all from $simpleName")
    dao.collection.remove(MongoDBObject())
  }
}

