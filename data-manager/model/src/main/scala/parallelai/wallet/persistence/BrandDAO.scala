package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{Brand, UserAccount}

import scala.concurrent.Future

/**
 * Created by pakkio on 29/09/2014.
 */
trait BrandDAO {

  def getById(brandId: UUID) : Future[Option[Brand]]

  def list : Future[List[Brand]]

  def insertNew(brand: Brand) : Future[Brand]

  def update(brand:Brand) : Future[Unit]

  def delete(brandId: UUID) : Future[Unit]



}
