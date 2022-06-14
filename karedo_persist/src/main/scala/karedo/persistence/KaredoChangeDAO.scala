package karedo.persistence

import java.util.UUID

import karedo.entity.{KaredoChangeDB, KaredoSales}

trait KaredoChangeDAO {

  def insertNew(newData: KaredoChangeDB): Option[UUID]
  def findById(id: UUID): Option[KaredoChangeDB]
  def findByCurrency(currency: String): Option[KaredoChangeDB]

}
