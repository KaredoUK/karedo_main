package parallelai.wallet.persistence.mongodb.offermanager

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import parallelai.wallet.persistence.mongodb.{MongoUserAccount, MongoConnection}
import com.novus.salat.dao.SalatDAO
import java.util.UUID
import parallelai.wallet.entity.dao.offermanager.RetailerDAO
import scala.concurrent.Future
import parallelai.wallet.entity.api.offermanager.Retailer

/**
 * Created by crajah on 06/07/2014.
 */
class RetailerDAOImpl( implicit val bindingModule: BindingModule ) extends RetailerDAO with MongoConnection with Injectable  {

//  val dao = new SalatDAO[MongoUserAccount, UUID](collection = db("UserAccount")) {}

  override def getByID(id: UUID): Future[Option[Retailer]] = ???

  override def insert(retailer: Retailer): Future[Unit] = ???

  override def deleter(id: UUID): Future[Unit] = ???

  override def update(retailer: Retailer): Future[Unit] = ???

  override def setName(id: UUID, name: String): Future[Unit] = ???

}
