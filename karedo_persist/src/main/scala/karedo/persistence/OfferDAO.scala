package karedo.persistence

import java.util.UUID

import karedo.entity.{Offer}

import scala.concurrent.Future

trait OfferDAO {


  def getById(id: UUID) : Option[Offer]

  def insertNew(offer: Offer) : Option[UUID]

  def delete(offerId: UUID) : Unit
}
