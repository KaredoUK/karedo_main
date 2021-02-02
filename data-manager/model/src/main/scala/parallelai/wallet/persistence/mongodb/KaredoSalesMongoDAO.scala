package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import parallelai.wallet.entity.KaredoSales
import parallelai.wallet.persistence._

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import parallelai.wallet.persistence.KaredoSalesDAO


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



  override def insertNew(t: KaredoSales): Option[UUID] =  {

    dao.insert( t )

  }
  override def findById(id: UUID): Option[KaredoSales] = {
    dao.findOneById(id)
  }
  override def findByCode(code: String): Option[KaredoSales] = {
    dao.findOne(MongoDBObject("code"->code))
  }



}
