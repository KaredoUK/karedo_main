package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.{AccountSettings, UserPersonalInfo, ClientApplication, UserAccount}
import scala.concurrent.Future

trait UserAccountDAO {

  def getById(userId: UUID) : Future[Option[UserAccount]]

  def getByMsisdn(msisdn: String, mustBeActive: Boolean = false) : Future[Option[UserAccount]]

  def getByEmail(email: String, mustBeActive: Boolean = false) : Future[Option[UserAccount]]

  def getByApplicationId(applicationId: UUID, mustBeActive: Boolean = false) : Future[Option[UserAccount]]

  def insertNew(userAccount: UserAccount, firstApplication: ClientApplication) : Future[Unit]

  def update(userAccount: UserAccount): Future[Unit]

  def updateSubInfo(id: UUID, userInfo: UserPersonalInfo, personalSettings: AccountSettings): Future[Unit]

  def setActive(userId : UUID): Future[Unit]

  def setEmail(userId: UUID, email: String) : Future[Unit]

  def setMsisdn(userId: UUID, msisdn: String) : Future[Unit]

  def setPassword(userId: UUID, password: String) : Future[Unit]

  def findByAnyOf(applicationId: Option[UUID], msisdn: Option[String], email: Option[String]) : Future[Option[UserAccount]]

  def delete(userId: UUID) : Future[Unit]
}


