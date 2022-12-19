package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import parallelai.wallet.entity.{Offer, Brand}
import parallelai.wallet.persistence.{OfferDAO, BrandDAO}

class OfferMongoDAO (implicit val bindingModule: BindingModule) extends OfferDAO with MongoConnection with Injectable   {

  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[Offer, UUID](collection = db("Offer")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)

  override def getById(id: UUID): Option[Offer] = dao.findOneById(id)

  override def insertNew(offer: Offer): Option[UUID] =  {
    val result = dao.insert( offer )
    result
  }

  override def delete(id: UUID): Unit = {
      dao.removeById(id, WriteConcern.Safe)
  }

}
