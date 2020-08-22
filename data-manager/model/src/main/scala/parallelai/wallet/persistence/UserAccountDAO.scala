package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.{AccountSettings, UserPersonalInfo, ClientApplication, UserAccount}
import parallelai.wallet.persistence.mongodb.SubscribedBrands
import scala.concurrent.Future

trait UserAccountDAO {


  def getById(userId: UUID) : Option[UserAccount]

  def getByMsisdn(msisdn: String, mustBeActive: Boolean = false) : Option[UserAccount]

  def getByEmail(email: String, mustBeActive: Boolean = false) : Option[UserAccount]

  def getByApplicationId(applicationId: UUID, mustBeActive: Boolean = false) : Option[UserAccount]

  def insertNew(userAccount: UserAccount, firstApplication: ClientApplication) : Unit

  def update(userAccount: UserAccount): Unit

  def updateSubInfo(id: UUID, userInfo: UserPersonalInfo, personalSettings: AccountSettings): Unit

  def setActive(userId : UUID): Unit

  def setEmail(userId: UUID, email: String) : Unit

  def setMsisdn(userId: UUID, msisdn: String) : Unit

  def setPassword(userId: UUID, password: String) : Unit

  def findByAnyOf(applicationId: Option[UUID], msisdn: Option[String], email: Option[String]) : Option[UserAccount]

  def delete(userId: UUID) : Unit

  def addBrand(userId: UUID, brandId: UUID): Unit

  def getBrand(userId: UUID, brandId: UUID) : Boolean

  def deleteBrand(userId: UUID, brandId: UUID): Unit

  def listUserSubscribedBrands(userId: UUID): List[SubscribedBrands]

}


