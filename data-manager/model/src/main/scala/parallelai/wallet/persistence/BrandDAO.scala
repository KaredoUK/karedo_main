package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.{Brand, UserAccount}

import scala.concurrent.Future

/**
 * Created by pakkio on 29/09/2014.
 */
trait BrandDAO {

  def getById(brandId: UUID) : Option[Brand]

  def list : List[Brand]

  def insertNew(brand: Brand) : Option[UUID]

  def update(brand:Brand) : Unit

  def delete(brandId: UUID) : Unit



}
