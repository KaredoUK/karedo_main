package parallelai.wallet.persistence

import java.util.UUID
import parallelai.wallet.entity.ClientApplication
import scala.concurrent.Future

trait ClientApplicationDAO {
  def getById(applicationId: UUID) : Future[Option[ClientApplication]]

  def findByUserId(userId: UUID) : Future[Seq[ClientApplication]]

  def update(clientApp: ClientApplication) : Future[Unit]

  def insertNew(clientApp: ClientApplication) : Future[Unit]
}
