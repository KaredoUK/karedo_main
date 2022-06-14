package karedo.persistence

import java.util.UUID

import karedo.entity.{KaredoLog, AdvertisementDetail, Brand}

trait LogDAO {


  def addLog(l: KaredoLog) : Option[UUID]
  def getById(id: UUID): Option[KaredoLog]

}


