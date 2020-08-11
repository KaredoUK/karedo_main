package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.mongodb.casbah.Imports._
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import parallelai.wallet.entity.{AdvertisementMetadata, UserPersonalInfo, AccountSettings, Brand}
import parallelai.wallet.persistence.BrandDAO

import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import com.novus.salat.dao._

import com.mongodb.casbah.Imports._


import scala.concurrent.Future
import scala.concurrent.Future._

/**
 * Created by pakkio on 29/09/2014.
 */
class MongoBrandDAO (implicit val bindingModule: BindingModule) extends BrandDAO with MongoConnection with Injectable   {

  val dao = new SalatDAO[Brand, UUID](collection = db("Brand")) {}

  def byId(brandId: UUID) = MongoDBObject("_id" -> brandId)

  override def getById(brandId: UUID): Future[Option[Brand]] = successful { dao.findOneById(brandId)}

  override def update(brand: Brand): Future[Unit] = successful {


    dao.update(
      byId(brand.id),
      $set(
       "name" -> brand.name,


        "iconPath" -> brand.iconPath,
        "ads" -> brand.ads.map { ad => grater[AdvertisementMetadata].asDBObject(ad)}

      )
    )
  }


  override def insertNew(brand: Brand): Future[Brand] = successful {
    val result = brand.copy(id = UUID.randomUUID() )
    dao.insert( result )
    result
  }

  override def delete(brandId: UUID): Future[Unit] = {
    successful {
      dao.removeById(brandId, WriteConcern.Safe)
    }
  }

  override def list: Future[List[Brand]] = successful {
      dao.find(MongoDBObject.empty).toList
  }
}
