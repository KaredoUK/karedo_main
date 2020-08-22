package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{AdvertisementDetail, AdvertisementMetadata, Brand, UserAccount}

import scala.concurrent.Future

/**
 * Created by pakkio on 29/09/2014.
 */
trait AdvertisementDetailDAO {
  def getById(detailId: UUID): Option[AdvertisementDetail]
  def insertNew(detail: AdvertisementDetail): Option[UUID]
  def delete(detailId: UUID): Unit
}
