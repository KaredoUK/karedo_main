package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.UserAccount
import scala.concurrent.Future

trait UserAccountDAO {

  def getById(userId: UUID) : Future[Option[UserAccount]]

  def getByMsisdn(msisdn: String, mustBeActive: Boolean = false) : Future[Option[UserAccount]]

  def getByEmail(email: String, mustBeActive: Boolean = false) : Future[Option[UserAccount]]

  def getByApplicationId(applicationId: UUID, mustBeActive: Boolean = false) : Future[Option[UserAccount]]

  def insertNew(userAccount: UserAccount) : Unit

  def update(userAccount: UserAccount): Unit

  def setActive(userId : UUID): Unit

  def findByAnyOf(applicationId: Option[UUID], msisdn: Option[String], email: Option[String]) : Future[Option[UserAccount]]
}


