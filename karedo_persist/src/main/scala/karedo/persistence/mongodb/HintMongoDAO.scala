package karedo.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat.dao.SalatDAO
import karedo.entity._
import karedo.persistence.HintDAO

import com.novus.salat.dao
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import com.mongodb.casbah.Imports._


/**
 * Created by pakkio on 29/09/2014.
 */
class HintMongoDAO (implicit val bindingModule: BindingModule)
  extends HintDAO with MongoConnection with Injectable   {



  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[Hint,UUID](collection = db("Hint")) {}


  override def clear() = {
    dao.collection.remove(MongoDBObject.empty)
  }

  override def insertNew(hint: Hint): Option[UUID] =  {

    val result = dao.insert( hint )
    result
  }


  override def suggestedNAdsForUserAndBrandLimited(user: UUID, brand: UUID, numAds: Int) = {
    val ret=dao.find(MongoDBObject("userId" -> user, "brandId" -> brand))
      .sort(orderBy = MongoDBObject("score" -> -1))
      .limit(numAds).toList
    ret
  }

  override def count:Long = {
    dao.count(DBObject())
  }
}
