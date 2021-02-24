package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{KaredoChangeDB, KaredoSales}

trait KaredoChangeDAO {

  def insertNew(newData: KaredoChangeDB): Option[UUID]
  def findById(id: UUID): Option[KaredoChangeDB]
  def findByCurrency(currency: String): Option[KaredoChangeDB]

}
