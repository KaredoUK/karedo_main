package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.mongodb.casbah.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import parallelai.wallet.entity.{AdvertisementDetail, Brand}
import parallelai.wallet.persistence.{AdvDAO, BrandDAO}

/**
 * Created by pakkio on 29/09/2014.
 */
class AdvMongoDAO (implicit val bindingModule: BindingModule) extends AdvDAO with MongoConnection with Injectable   {

  val dao = new SalatDAO[AdvertisementDetail, UUID](collection = db("AdvertisementDetail")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)

  override def getById(id: UUID): Option[AdvertisementDetail] = dao.findOneById(id)



  override def insertNew(ad: AdvertisementDetail): Option[UUID] =  {

    val result = dao.insert( ad )
    result
  }

  override def delete(id: UUID): Unit = {
      dao.removeById(id, WriteConcern.Safe)
  }


}
