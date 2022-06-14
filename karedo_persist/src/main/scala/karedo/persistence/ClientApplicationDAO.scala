package karedo.persistence

import java.util.UUID
import karedo.entity.ClientApplication
import scala.concurrent.Future

trait ClientApplicationDAO {
  def getById(applicationId: UUID) : Option[ClientApplication]

  def findByUserId(userId: UUID) : Seq[ClientApplication]

  def update(clientApp: ClientApplication) : Unit

  def insertNew(clientApp: ClientApplication) : Unit
}
