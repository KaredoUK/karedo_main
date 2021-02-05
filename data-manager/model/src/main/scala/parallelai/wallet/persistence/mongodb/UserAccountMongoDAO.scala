package parallelai.wallet.persistence.mongodb

import parallelai.wallet.entity.KaredoTypes.KaredoPoints
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import scala.concurrent.Future
import scala.concurrent.Future._
import parallelai.wallet.entity._
import java.util.UUID
import com.novus.salat.dao.SalatDAO
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.novus.salat._
import com.novus.salat.global._


object userAccountMongoUtils {
  def byId(userId: UUID) = MongoDBObject("_id" -> userId)

  def bySubscribedBrands(brandId: UUID) = MongoDBObject("subscribedBrands" -> brandId)

  def byApplicationId(applicationId: UUID) = "applications" $elemMatch MongoDBObject("_id" -> applicationId)
}

import userAccountMongoUtils._


class UserAccountMongoDAO(implicit val bindingModule: BindingModule)
  extends UserAccountDAO
  with MongoConnection
  with Injectable
{
  implicit def mongoUserAccountToUserAccount(mongoUserAccount: MongoUserAccount): UserAccount = mongoUserAccount.toUserAccount

  implicit def mongoUserAccountOptionToUserAccountOption(mongoUserAccount: Option[MongoUserAccount]): Option[UserAccount] = mongoUserAccount map {
    _.toUserAccount
  }

  val dao = new SalatDAO[MongoUserAccount, UUID](collection = db("UserAccount")) {}
  //val brandDao = new SalatDAO[Brand, UUID](collection = db("Brand")) {}

  override def getById(userId: UUID): Option[UserAccount] = {
    val dbuser=dao.findOneById(userId)
    dbuser
  }

  override def consume(userId: UUID, points: KaredoPoints) : Unit = {
    dao.update(
      byId(userId),
      $inc("totalPoints" -> -points)
    )
  }

  override def update(userAccount: UserAccount): Unit =
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

  def updateSubInfo(id: UUID, userInfo: UserPersonalInfo, personalSettings: AccountSettings): Unit =
      dao.update(
        byId(id),
        $set(
          "personalInfo" -> userInfo,
          "settings" -> personalSettings
        )
      )

  override def getByMsisdn(msisdn: String, mustBeActive: Boolean): Option[UserAccount] =
      dao.findOne(MongoDBObject("msisdn" -> Some(msisdn)))

  override def setEmail(userId: UUID, email: String): Unit =
      dao.update(
        byId(userId),
        $set(
          "email" -> email
        )
      )


  override def insertNew(userAccount: UserAccount, firstApplication: ClientApplication): Unit =
      dao.insert(
        MongoUserAccount(userAccount.id, userAccount.msisdn, userAccount.email, userAccount.userType,
          userAccount.personalInfo, userAccount.settings, userAccount.password, userAccount.active, userAccount.totalPoints,
          List(),
          List(
            MongoUserApplicationInfo(firstApplication.id, firstApplication.activationCode, firstApplication.active)
          )
        )
      )



  override def addPoints(userId: UUID, points: KaredoPoints): Option[UserAccountTotalPoints] = {
    val query: DBObject = byId(userId)
    val updateQuery: DBObject = MongoDBObject("$inc" -> MongoDBObject("totalPoints" -> points))
    val fields: DBObject = MongoDBObject("totalPoints" -> "1")

    val ret=dao.collection.findAndModify(
      query, fields=fields, sort=null, remove=false, updateQuery, returnNew=true, upsert=false) match {
      case Some(dbo) =>
        Some(grater[UserAccountTotalPoints].asObject(dbo))
      case None =>
        None
    }
    ret

  }
/*
  @param query query to match
  * @param fields fields to be returned
    * @param sort sort to apply before picking first document
  * @param remove if true, document found will be removed
  * @param update update to apply
  * @param returnNew if true, the updated document is returned, otherwise the old document is returned (or it would be lost forever)
  * @param upsert do upsert*/



  def insertNew(userAccount: UserAccount, firstApplications: ClientApplication*): Unit =
      dao.insert(
        MongoUserAccount(userAccount.id, userAccount.msisdn, userAccount.email, userAccount.userType,
          userAccount.personalInfo, userAccount.settings, userAccount.password, userAccount.active, userAccount.totalPoints,
          List(),
          firstApplications map { app => MongoUserApplicationInfo(app.id, app.activationCode, app.active)} toList
        )
      )

  override def getByApplicationId(applicationId: UUID, mustBeActive: Boolean): Option[UserAccount] = {
      val query = if (mustBeActive) {
        $and(byApplicationId(applicationId), MongoDBObject("active" -> true))
      } else {
        byApplicationId(applicationId)
      }

      dao.findOne(query)
    }

  override def findByAnyOf(applicationId: Option[UUID], msisdn: Option[String], email: Option[String]): Option[UserAccount] = {
      val byAppIdOpt = applicationId map { value => byApplicationId(value)}
      val byMsisdnOpt = msisdn map { value => MongoDBObject("msisdn" -> value)}
      val byEmailOpt = email map { value => MongoDBObject("email" -> value)}

      val queryOp = List(byAppIdOpt, byMsisdnOpt, byEmailOpt).foldLeft[Option[DBObject]](None) { (queryAccumulator, currentQuery) => (queryAccumulator, currentQuery) match {
        case (Some(queryAcc), Some(currQuery)) => Some($or(queryAcc, currQuery))
        case (Some(_), None) => queryAccumulator
        case (None, Some(_)) => currentQuery
        case (None, None) => None
      }
      }

      queryOp flatMap { query => dao.findOne(query) map {
        _.toUserAccount
      }
      }
    }


  override def setActive(userId: UUID): Unit =
      dao.update(
        byId(userId),
        $set("active" -> true)
      )

  override def setMsisdn(userId: UUID, msisdn: String): Unit =
      dao.update(
        byId(userId),
        $set(
          "msisdn" -> msisdn
        )
      )

  override def setPassword(userId: UUID, password: String): Unit =
      dao.update(
        byId(userId),
        $set(
          "password" -> password
        )
      )

  override def checkPassword(userId: UUID, password: String): Boolean =
  {
    val element = dao.findOne($and(byId(userId),MongoDBObject("password"->password)))
    element match {
      case Some(_) => true
      case _ => false
    }
  }

  override def getByEmail(email: String, mustBeActive: Boolean ) : Option[UserAccount] = {
      val query = if (mustBeActive) {
        $and(MongoDBObject("email" -> Some(email)), MongoDBObject("active" -> true))
      } else {
        MongoDBObject("email" -> Some(email))
      }

      dao.findOne(query)
    }

  override def delete(userId: UUID): Unit =
      dao.removeById(userId, WriteConcern.Safe)

  override def addBrand(userId: UUID, brandId: UUID): Unit =
      dao.update(byId(userId),
        $push("subscribedBrands" -> brandId)
      )

  override def getBrand(userId: UUID, brandId: UUID) : Boolean = {
      val query = $and(byId(userId), bySubscribedBrands(brandId))
      dao.findOne(query) match {
        case Some(_) => true
        case None => false
      }
    }

  override def deleteBrand(userId: UUID, brandId: UUID): Unit =
    try {
      dao.update(
        byId(userId),
        $pull("subscribedBrands" -> brandId)
      )
    } catch {
      case e : Throwable =>
        e.printStackTrace()
    }

  override def listUserSubscribedBrands(userId: UUID): List[UUID] = {
    val list=getById(userId) match {
      case None => List()
      case Some(account) => account.subscribedBrands
    }
    list
  }
}

class ClientApplicationMongoDAO(implicit val bindingModule: BindingModule)
  extends ClientApplicationDAO
  with MongoConnection
  with Injectable
{
  val dao = new SalatDAO[MongoUserAccount, UUID](collection = db("UserAccount")) {}

  override def getById(applicationId: UUID): Option[ClientApplication] =
      dao.findOne(byApplicationId(applicationId)) flatMap {
        account =>
          account.applications.find(_.id == applicationId).map {
            _.toClientApplication(account.id)
          }
      }

  override def findByUserId(userId: UUID): Seq[ClientApplication] =
      dao.projections[MongoUserApplicationInfo](byId(userId), "applications") map {
        _.toClientApplication(userId)
      }

  override def update(clientApp: ClientApplication): Unit =
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

  override def insertNew(clientApp: ClientApplication): Unit =
      dao.update(
        byId(clientApp.accountId),
        $push(
          "applications" -> grater[MongoUserApplicationInfo].asDBObject(MongoUserApplicationInfo.fromClientApplication(clientApp))
          // Don't use the MongoUserApplicationInfo direct otherwise will try to add a list of Any into applications and fuck up the object
        )
      )
}