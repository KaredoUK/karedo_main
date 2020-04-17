package parallelai.wallet.persistence.mongodb

import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import scala.concurrent.Future
import scala.concurrent.Future._
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import java.util.UUID
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports._
import parallelai.wallet.entity.ClientApplication
import parallelai.wallet.entity.UserAccount
import parallelai.wallet.entity.ClientApplication
import parallelai.wallet.entity.UserAccount
import com.novus.salat.global._
import com.novus.salat.dao._

import com.mongodb.casbah.Imports._
import parallelai.wallet.entity.UserAccount
import parallelai.wallet.entity.ClientApplication
import scala.Some
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._


object userAccountMongoUtils {
  def byId(userId: UUID) = MongoDBObject("_id" -> userId)

  def byApplicationId(applicationId: UUID) = "applications" $elemMatch MongoDBObject( "_id" -> applicationId )
}

import userAccountMongoUtils._

trait MongoConnection {
  def mongoHost: String
  def mongoPort: Int
  def mongoDbName: String

  val mongoClient = MongoClient(mongoHost, mongoPort)
  val db = mongoClient(mongoDbName)
}

class UserAccountMongoDAO(implicit val bindingModule: BindingModule) extends UserAccountDAO with MongoConnection with Injectable  {

  lazy val mongoHost: String = injectProperty[String]("mongo.server.host")
  lazy val mongoPort: Int = injectProperty[Int]("mongo.server.port")
  lazy val mongoDbName: String = injectProperty[String]("mongo.db.name")

  implicit def mongoUserAccountToUserAccount(mongoUserAccount: MongoUserAccount) : UserAccount = mongoUserAccount.toUserAccount
  implicit def mongoUserAccountOptionToUserAccountOption(mongoUserAccount: Option[MongoUserAccount]) : Option[UserAccount] = mongoUserAccount map { _.toUserAccount }

  val dao = new SalatDAO[MongoUserAccount, UUID](collection = db("UserAccount")) {}

  override def getById(userId: UUID): Future[Option[UserAccount]] = successful { dao.findOneById(userId) }

  override def update(userAccount: UserAccount): Future[Unit] =
   successful {
    dao.update(
      byId(userAccount.id),
      $set(
        "active" -> userAccount.active,
        "email" -> userAccount.email,
        "msisdn" -> userAccount.msisdn,
        "personalInfo" -> userAccount.personalInfo,
        "settings" -> userAccount.settings
      )
    )
  }

  override def getByMsisdn(msisdn: String, mustBeActive: Boolean): Future[Option[UserAccount]] =
    successful {
      dao.findOne( MongoDBObject( "msisdn" -> Some(msisdn) ) )
    }

  override def setEmail(userId: UUID, email: String): Future[Unit] =
    successful {
      dao.update(
        byId(userId),
        $set(
          "email" -> email
        )
      )
    }

  override def insertNew(userAccount: UserAccount, firstApplication: ClientApplication): Future[Unit] =
    successful {
      dao.insert(
        MongoUserAccount(userAccount.id, userAccount.msisdn, userAccount.email, userAccount.personalInfo, userAccount.settings, userAccount.active,
          List(
            MongoUserApplicationInfo(firstApplication.id, firstApplication.activationCode, firstApplication.active)
          )
        )
      )
    }

  override def getByApplicationId(applicationId: UUID, mustBeActive: Boolean): Future[Option[UserAccount]] =
    successful{
      val query = if(mustBeActive) {
        $and(byApplicationId(applicationId), MongoDBObject( "active" -> true) )
      } else {
        byApplicationId(applicationId)
      }

      dao.findOne(query)
    }

  override def findByAnyOf(applicationId: Option[UUID], msisdn: Option[String], email: Option[String]): Future[Option[UserAccount]] =
    successful {
      val byAppIdOpt = applicationId map { value => MongoDBObject( "applications.id" -> value ) }
      val byMsisdnOpt = msisdn map { value => MongoDBObject( "msisdn" -> value ) }
      val byEmailOpt = email map { value => MongoDBObject( "email" -> value ) }

      val queryOp = List(byAppIdOpt, byMsisdnOpt, byEmailOpt).foldLeft[Option[DBObject]](None) { (queryAccumulator, currentQuery) => (queryAccumulator, currentQuery) match {
        case (Some(queryAcc), Some(currQuery)) => Some( $or(queryAcc, currQuery) )
        case (Some(_), None) => queryAccumulator
        case (None, Some(_)) => currentQuery
        case (None, None) => None
        }
      }

      queryOp flatMap { query => dao.findOne(query) map { _.toUserAccount } }
    }


  override def setActive(userId: UUID): Future[Unit] =
    successful {
      dao.update(
        byId(userId),
        $set("active" -> true)
      )
    }

  override def setMsisdn(userId: UUID, msisdn: String): Future[Unit] =
    successful {
      dao.update(
        byId(userId),
        $set(
          "msisdn" -> msisdn
        )
      )
    }

  override def getByEmail(email: String, mustBeActive: Boolean): Future[Option[UserAccount]] =
    successful {
      dao.findOne( MongoDBObject( "email" -> Some(email) ) )
    }
}


class ClientApplicationMongoDAO(implicit val bindingModule: BindingModule)  extends ClientApplicationDAO with MongoConnection with Injectable {
  lazy val mongoHost: String = injectProperty[String]("mongo.server.host")
  lazy val mongoPort: Int = injectProperty[Int]("mongo.server.port")
  lazy val mongoDbName: String = injectProperty[String]("mongo.db.name")

  val dao = new SalatDAO[MongoUserAccount, UUID](collection = db("UserAccount")) {}

  override def getById(applicationId: UUID): Future[Option[ClientApplication]] =
    successful{
      dao.findOneById(applicationId) flatMap {  account => account.applications.find(_.id == applicationId).map( _.toClientApplication(account.id) ) }
    }

  override def findByUserId(userId: UUID): Future[Seq[ClientApplication]] =
    successful {
      dao.projections[MongoUserApplicationInfo](byId(userId), "applications") map { _.toClientApplication(userId)}
    }

  override def update(clientApp: ClientApplication): Future[Unit] =
    successful {
      dao.update(
        MongoDBObject(
          "id" -> clientApp.accountId,
          "applications._id" -> clientApp.id
        ),
        $set(
          "applications.$.activationCode" -> clientApp.activationCode,
          "applications.$.active" -> clientApp.active
        )
      )
    }

  override def insertNew(clientApp: ClientApplication): Future[Unit] =
    successful {
      dao.update(
        byId(clientApp.accountId),
        $push(
          "applications.$._id" -> clientApp.id,
          "applications.$.activationCode" -> clientApp.activationCode,
          "applications.$.active" -> clientApp.active
        )
      )
    }
}