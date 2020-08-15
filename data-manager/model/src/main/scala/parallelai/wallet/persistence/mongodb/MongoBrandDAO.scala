package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}


import parallelai.wallet.entity.{AdvertisementMetadata, UserPersonalInfo, AccountSettings, Brand}
import parallelai.wallet.persistence.BrandDAO



import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import com.mongodb.casbah.Imports._



import scala.concurrent.Future
import scala.concurrent.Future._

/**
 * Created by pakkio on 29/09/2014.
 */
class MongoBrandDAO (implicit val bindingModule: BindingModule) extends BrandDAO with MongoConnection with Injectable   {

  val dao = new SalatDAO[Brand, UUID](collection = db("Brand")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)

  override def getById(id: UUID): Option[Brand] = dao.findOneById(id)

  override def update(brand: Brand): Unit =

    dao.update(
      byId(brand.id),
      $set(
       "name" -> brand.name,
        "iconPath" -> brand.iconPath,
        "ads" -> brand.ads.map { ad => grater[AdvertisementMetadata].asDBObject(ad)}

      )
    )


  override def insertNew(brand: Brand): Brand =
  {
    dao.insert( brand )
    brand
  }

  override def delete(id: UUID): Unit = {

      dao.removeById(id, WriteConcern.Safe)

  }

  override def list: List[Brand] =
      dao.find(MongoDBObject.empty).toList

}
