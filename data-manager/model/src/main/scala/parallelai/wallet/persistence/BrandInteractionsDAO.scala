package parallelai.wallet.persistence

import java.util.UUID

import org.joda.time.DateTime
import parallelai.wallet.persistence.InteractionType._

object InteractionType {
  val Like = "Like"
  val Click = "Click"
}


case class Interaction(id: UUID=UUID.randomUUID(), datetime: DateTime=new DateTime(), userId: UUID, kind: String, note: String )


trait BrandInteractionsDAO {
  def clear(): Unit
  def getById(interactionId: UUID): Option[Interaction]
  //def findByUserId(userId: UUID): Seq[BrandInteraction]
  def delete(interactionId: UUID): Unit
  def insertNew(brandId: UUID, interaction: Interaction): Unit
  def getInteractions(brandId: UUID): List[Interaction]
}