package parallelai.wallet.persistence.db.ua

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import org.slf4j.LoggerFactory
import parallelai.wallet.entity.KaredoTypes.KaredoPoints
import parallelai.wallet.entity._
import parallelai.wallet.persistence.db.ua.userAccountMongoUtils._
import parallelai.wallet.persistence.mongodb.MongoConnection

import scala.util.Try


object userAccountMongoUtils {
  def byId(userId: UUID) = MongoDBObject("_id" -> userId)

}


class UserAccountMongoDAO(implicit val bindingModule: BindingModule)
  extends UserAccountDAO
    with MongoConnection
    with Injectable {


  val logger = LoggerFactory.getLogger(classOf[UserAccountMongoDAO])
  logger.info("setting up UserAccountMongoDAO")

  val dao = new SalatDAO[UserAccount, UUID](collection = db("XUserAccount")) {}

  override def insertNew(userAccount: UserAccount): Try[Option[UUID]] = {
    logger.info(s"insertNew $userAccount")
    Try(
      dao.insert(
        userAccount
      )
    )
  }

  override def getById(id: UUID) = {
    logger.info(s"getById $id")
    val dbuser = dao.findOneById(id)
    logger.info(s"getById returning $dbuser")

    dbuser
  }

  override def update(userAccount: UserAccount): Try[WriteResult] = {
    logger.info(s"updating $userAccount")
    Try {
      dao.update(byId(userAccount.id), grater[UserAccount].asDBObject(userAccount))
    }
  }

  override def delete(userId: UUID): Try[WriteResult] = {
    logger.info(s"deleting userId: $userId")
    Try {
      dao.removeById(userId)
    }
  }
}

