package karedo.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import org.joda.time.DateTime
import karedo.entity.{KaredoChangeDB, KaredoSales}
import karedo.persistence.{KaredoChangeDAO, KaredoSalesDAO}


/**
 * Created by pakkio on 29/09/2014.
 */
class KaredoChangeMongoDAO (implicit val bindingModule: BindingModule)
  extends KaredoChangeDAO
  with MongoConnection
  with Injectable
{



  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[KaredoChangeDB,UUID](collection = db("KaredoChange")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)
  def byCurrency(currency: String) = MongoDBObject("currency"->currency)

  override def insertNew(t: KaredoChangeDB): Option[UUID] =  {
    // remove previous one if exists
    findByCurrency(t.currency) match {
      case Some(change) => dao.remove(byId(change.id))
      case _ =>
    }
    dao.insert( t )

  }
  override def findById(id: UUID): Option[KaredoChangeDB] = {
    dao.findOneById(id)
  }
  override def findByCurrency(currency: String): Option[KaredoChangeDB] = {
    // returns only sales with codes not consumed

    dao.findOne(MongoDBObject("currency"->currency))
  }
}
