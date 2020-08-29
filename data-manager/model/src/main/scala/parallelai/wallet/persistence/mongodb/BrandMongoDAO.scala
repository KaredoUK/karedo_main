package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers


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
class BrandMongoDAO (implicit val bindingModule: BindingModule) extends BrandDAO with MongoConnection with Injectable   {

  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[Brand, UUID](collection = db("Brand")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)

  override def getById(id: UUID): Option[Brand] = dao.findOneById(id)

  override def delAdvertisement(brandId: UUID, id: UUID): Unit = {
    dao.update(byId(brandId), $pull("ads" -> MongoDBObject("detailId" -> id)))
    None

  }


  override def update(brand: Brand): Unit = {
    dao.update(
      byId(brand.id),
      $set(
        "name" -> brand.name,
        "iconPath" -> brand.iconPath,
        "ads" -> brand.ads.map { ad => grater[AdvertisementMetadata].asDBObject(ad)}

      )
    )
  }


  override def insertNew(brand: Brand): Option[UUID] =  {

    val result = dao.insert( brand )
    result
  }

  override def delete(id: UUID): Unit = {
      dao.removeById(id, WriteConcern.Safe)
  }

  override def list: List[Brand] = {
      dao.find(MongoDBObject.empty).toList
  }

  override def addAdvertisement(brandId: UUID, adv: AdvertisementMetadata): Unit = {
      dao.update(
        byId(brandId),
        $push("ads" -> grater[AdvertisementMetadata].asDBObject(adv))
      )
  }

  override def listAds(brandId: UUID) = {
    dao.findOneById(brandId) match {
      case Some(b) => b.ads
      case None => List[AdvertisementMetadata]()
    }

  }
}
