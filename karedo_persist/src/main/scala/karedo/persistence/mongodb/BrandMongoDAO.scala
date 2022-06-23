package karedo.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import org.joda.time.{DateTime, Interval}


import karedo.entity._
import karedo.persistence.BrandDAO


import com.novus.salat.dao
import com.novus.salat._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._
import com.mongodb.casbah.Imports._







import scala.concurrent.Future
import scala.concurrent.Future._

class BrandMongoDAO (implicit val bindingModule: BindingModule)
  extends BrandDAO with MongoConnection with Injectable   {

  //This should be removed and using the MongoAppSupport features
  RegisterJodaTimeConversionHelpers()
  val dao = new SalatDAO[Brand, UUID](collection = db("Brand")) {}

  def byId(id: UUID) = MongoDBObject("_id" -> id)
  def adById(id: UUID) = MongoDBObject("ads._id" -> id)

  override def getById(id: UUID): Option[Brand] = dao.findOneById(id)

  override def delAd(id: UUID): Unit = {
    dao.update(adById(id), $pull("ads" -> MongoDBObject("_id" -> id)))
    None

  }
  override def getAdById(adId: UUID): Option[AdvertisementDetail] = {
    dao.findOne(adById(adId)).flatMap{
      brand =>
        brand.ads.find( _.id == adId)
    }
  }


  override def update(brand: Brand): Unit = {
    dao.update(
      byId(brand.id),
      $set(
        "name" -> brand.name,
        "iconId" -> brand.iconId,
        "ads" -> brand.ads.map { ad => grater[AdvertisementDetail].asDBObject(ad)}

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

  override def addAd(brandId: UUID, ad: AdvertisementDetail): Unit = {
      dao.update(
        byId(brandId),
        $push("ads" -> grater[AdvertisementDetail].asDBObject(ad))
      )
  }

  override def listAds(brandId: UUID, max:Int=0): List[AdvertisementDetail] = {
    dao.findOneById(brandId) match {
      case Some(brand) =>
        if(max>0) brand.ads.take(max) else brand.ads
      case _ =>
        List[AdvertisementDetail]()
    }

  }

  override def listActiveAds(brandId: UUID): List[AdvertisementDetail] = {
    listAds(brandId,0) filter {
      ad =>
        val interval=new Interval(ad.startDate,ad.endDate)
        interval.contains(DateTime.now())
    }
  }


}
