package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import parallelai.wallet.entity.Sale
import parallelai.wallet.persistence.SaleDAO

class SaleMongoDAO (implicit val bindingModule: BindingModule) extends SaleDAO
    with MongoConnection with Injectable   {

  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[Sale, UUID](collection = db("Sale")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)

  override def getById(id: UUID): Option[Sale] = dao.findOneById(id)

  override def insertNew(sale: Sale): Option[UUID] =  {
    val result = dao.insert( sale )
    result
  }

  override def getByCode(code: String): Option[Sale] = None

  override def redeem(id: UUID): Unit = Unit

}
