package parallelai.wallet.persistence.mongodb

import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import scala.concurrent.Future
import scala.concurrent.Future._
import parallelai.wallet.entity._
import java.util.UUID
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import com.novus.salat.dao._

import com.mongodb.casbah.Imports._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.global._


object userAccountMongoUtils {
  def byId(userId: UUID) = MongoDBObject("_id" -> userId)

  def byApplicationId(applicationId: UUID) = "applications" $elemMatch MongoDBObject( "_id" -> applicationId )
}

import userAccountMongoUtils._



class UserAccountMongoDAO(implicit val bindingModule: BindingModule) extends UserAccountDAO with MongoConnection with Injectable  {
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
        "personalInfo" -> grater[UserPersonalInfo].asDBObject(userAccount.personalInfo),
        "settings" -> grater[AccountSettings].asDBObject(userAccount.settings)
      )
    )
  }

  def updateSubInfo(id: UUID, userInfo: UserPersonalInfo, personalSettings: AccountSettings): Future[Unit] =
    successful {
      dao.update(
        byId(id),
        $set(
          "personalInfo" -> userInfo,
          "settings" -> personalSettings
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
        MongoUserAccount(userAccount.id, userAccount.msisdn, userAccount.email,
          userAccount.personalInfo, userAccount.settings, userAccount.active, userAccount.totalPoints,
          List(
            MongoUserApplicationInfo(firstApplication.id, firstApplication.activationCode, firstApplication.active)
          )
        )
      )
    }

  def insertNew(userAccount: UserAccount, firstApplications: ClientApplication* ): Future[Unit] =
    successful {
      dao.insert(
        MongoUserAccount(userAccount.id, userAccount.msisdn, userAccount.email,
          userAccount.personalInfo, userAccount.settings, userAccount.active, userAccount.totalPoints,
          firstApplications map { app => MongoUserApplicationInfo(app.id, app.activationCode, app.active) } toList
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
      val byAppIdOpt = applicationId map { value => byApplicationId(value) }
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

  override def setPassword(userId: UUID, password: String) : Future[Unit] =
    successful {
      dao.update(
        byId(userId),
        $set(
          "password" -> password
        )
      )
    }

  override def getByEmail(email: String, mustBeActive: Boolean): Future[Option[UserAccount]] =
    successful {
      val query = if(mustBeActive) {
        $and(MongoDBObject( "email" -> Some(email) ), MongoDBObject( "active" -> true) )
      } else {
        MongoDBObject( "email" -> Some(email) )
      }

      dao.findOne( query )
    }

  override def delete(userId: UUID) : Future[Unit] =
    successful {
      dao.removeById(userId, WriteConcern.Safe)
    }

  override def addBrand(userId: UUID, brandId: UUID): Future[Unit] =
  successful {
    dao.update(byId(userId),
      $push("subscribedBrands" -> brandId)
    )
  }

  override def deleteBrand(userId: UUID, brandId: UUID): Future[Unit] =
  successful {
    dao.update(
      byId(userId),
      $pull("subscribedBrands.0" -> brandId)
    )
  }

  override def listUserSubscribedBrands(userId: UUID): Future[List[SubscribedBrands]] =
  successful {
    dao.projections[SubscribedBrands](byId(userId),"subscribedBrands")
  }
}

class ClientApplicationMongoDAO(implicit val bindingModule: BindingModule)  extends ClientApplicationDAO with MongoConnection with Injectable {
  val dao = new SalatDAO[MongoUserAccount, UUID](collection = db("UserAccount")) {}

  override def getById(applicationId: UUID): Future[Option[ClientApplication]] =
    successful{
      dao.findOne( byApplicationId(applicationId) ) flatMap {
        account =>
          account.applications.find(_.id == applicationId).map { _.toClientApplication(account.id) }
      }
    }

  override def findByUserId(userId: UUID): Future[Seq[ClientApplication]] =
    successful {
      dao.projections[MongoUserApplicationInfo](byId(userId), "applications") map { _.toClientApplication(userId)}
    }

  override def update(clientApp: ClientApplication): Future[Unit] =
    successful {
      dao.update(
        MongoDBObject(
          "_id" -> clientApp.accountId,
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
          "applications" -> grater[MongoUserApplicationInfo].asDBObject( MongoUserApplicationInfo.fromClientApplication(clientApp) )
          // Don't use the MongoUserApplicationInfo direct otherwise will try to add a list of Any into applications and fuck up the object
        )
      )
    }
}