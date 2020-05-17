package parallelai.wallet.entity.dao.offermanager

import parallelai.wallet.entity.api.offermanager.Retailer
import scala.concurrent.Future
import java.util.UUID

/**
 * Created by crajah on 30/06/2014.
 */
trait RetailerDAO {
  def getByID(id: UUID): Future[Option[Retailer]]

  def insert(retailer: Retailer): Future[Unit]

  def deleter(id: UUID): Future[Unit]

  def update(retailer: Retailer): Future[Unit]

  def setName(id: UUID, name: String): Future[Unit]
}
