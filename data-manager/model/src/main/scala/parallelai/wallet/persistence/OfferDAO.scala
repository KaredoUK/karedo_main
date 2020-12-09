package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{Offer}

import scala.concurrent.Future

trait OfferDAO {
  def getById(id: UUID) : Option[Offer]

  def insertNew(offer: Offer) : Option[UUID]

  def delete(offerId: UUID) : Unit
}
