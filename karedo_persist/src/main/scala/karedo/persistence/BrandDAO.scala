package karedo.persistence

import java.util.UUID

import karedo.entity.{AdvertisementDetail, Brand, UserAccount, SuggestedAdForUsersAndBrandModel}

import scala.concurrent.Future


trait BrandDAO {


  def getById(id: UUID) : Option[Brand]
  def list : List[Brand]
  def insertNew(brand: Brand) : Option[UUID]
  def update(brand:Brand) : Unit
  def delete(brandId: UUID) : Unit


  def addAd(brandId: UUID, ad: AdvertisementDetail): Unit
  def listAds(brandId:UUID,max:Int): List[AdvertisementDetail]
  def listActiveAds(brandID:UUID): List[AdvertisementDetail]
  def delAd(uuid: UUID): Unit
  def getAdById(adId: UUID): Option[AdvertisementDetail]


}


