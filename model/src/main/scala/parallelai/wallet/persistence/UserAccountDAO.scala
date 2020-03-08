package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.UserAccount

trait UserAccountDAO {

  def getById(userId: UUID) : Option[UserAccount]

  def getByApplicationId(applicationId: UUID) : Option[UserAccount]

  def insertNew(userAccount: UserAccount) : Unit

  def update(userAccount: UserAccount): Unit
}
