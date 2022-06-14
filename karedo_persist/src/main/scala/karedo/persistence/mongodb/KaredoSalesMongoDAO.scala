package karedo.persistence.mongodb

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import karedo.entity.KaredoSales
import karedo.persistence._

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import karedo.persistence.KaredoSalesDAO


/**
 * Created by pakkio on 29/09/2014.
 */
class KaredoSalesMongoDAO (implicit val bindingModule: BindingModule)
  extends KaredoSalesDAO
  with MongoConnection
  with Injectable
{



  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[KaredoSales,UUID](collection = db("KaredoSales")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)
  def byCode(code: String) = MongoDBObject("code"->code)
  def notConsumed = "dateConsumed" -> MongoDBObject("$exists" -> 0)

  override def insertNew(t: KaredoSales): Option[UUID] =  {

    dao.insert( t )

  }

  override def getAcceptedOffers(uid: UUID): List[KaredoSales] = {
    dao.find(MongoDBObject(
      "accountId" -> uid,
      "saleType" -> "OFFER",
      notConsumed)).toList
  }
  override def findById(id: UUID): Option[KaredoSales] = {
    dao.findOneById(id)
  }
  override def findByOffer(offerId: UUID): Option[KaredoSales] = {
    dao.findOne(MongoDBObject("adId" -> offerId))
  }
  override def findByCode(code: String): Option[KaredoSales] = {
    // returns only sales with codes not consumed

    dao.findOne(MongoDBObject("code"->code, notConsumed))
  }
  override def consume(code: String): Option[KaredoSales] = {
    findByCode(code) match {
      case Some(sale) =>
        dao.update(byId(sale.id), MongoDBObject("$set" -> MongoDBObject("dateConsumed" -> new DateTime())))
        findById(sale.id)
      case None => None
    }
  }

 override def complete(id: UUID): Option[KaredoSales] = {
    findById(id) match {
      case Some(sale) =>
        dao.update(byId(sale.id), MongoDBObject("$set" -> MongoDBObject("dateConsumed" -> new DateTime())))
        findById(sale.id)
      case None => None
    }
  }




}
