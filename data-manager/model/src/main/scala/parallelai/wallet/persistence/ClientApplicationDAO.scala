package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.ClientApplication
import scala.concurrent.Future

trait ClientApplicationDAO {
  def getById(applicationId: UUID) : Option[ClientApplication]

  def findByUserId(userId: UUID) : Seq[ClientApplication]

  def update(clientApp: ClientApplication) : Unit

  def insertNew(clientApp: ClientApplication) : Unit
}
