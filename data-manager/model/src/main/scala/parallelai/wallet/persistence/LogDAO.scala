package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{KaredoLog, AdvertisementDetail, Brand}

trait LogDAO {


  def addLog(l: KaredoLog) : Option[UUID]
  def getById(id: UUID): Option[KaredoLog]

}


