package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import parallelai.wallet.entity._
import parallelai.wallet.persistence.HintDAO


/**
 * Created by pakkio on 29/09/2014.
 */
class InteractionsMongoDAO (implicit val bindingModule: BindingModule)
  extends InteractionsDAO with MongoConnection with Injectable   {



  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[Interaction,UUID](collection = db("Interaction")) {}


  override def clear() = {
    dao.collection.remove(MongoDBObject.empty)
  }

  override def insertNew(interaction: Interaction): Option[UUID] =  {

    val result = dao.insert( interaction )
    result
  }



}

class Interaction
