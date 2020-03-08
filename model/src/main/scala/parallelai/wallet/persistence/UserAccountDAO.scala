package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.UserAccount
import scala.concurrent.Future

trait UserAccountDAO {

  def getById(userId: UUID) : Future[Option[UserAccount]]

  def getByMsisdn(msisdn: String) : Future[Option[UserAccount]]

  def getByEmail(email: String) : Future[Option[UserAccount]]

  def getByApplicationId(applicationId: UUID) : Future[Option[UserAccount]]

  def insertNew(userAccount: UserAccount) : Unit

  def update(userAccount: UserAccount): Unit
}
