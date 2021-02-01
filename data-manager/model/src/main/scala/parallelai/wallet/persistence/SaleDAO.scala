package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{Sale, Offer}

trait SaleDAO {
  def getById(id: UUID) : Option[Sale]

  def insertNew(sale: Sale) : Option[UUID]

  def getByCode(code: String): Option[Sale]

  def redeem(id: UUID): Unit
}
