package karedo.route.common

import karedo.persist.entity._

/**
  * Created by crajah on 11/10/2016.
  */
trait DbCollections {
  val dbAds = new DbAds {}
  val dbKaredoChange = new DbKaredoChange {}
  val dbPrefs = new DbPrefs {}
  val dbUserAccount = new DbUserAccount {}
  val dbUserAd = new DbUserAd {}
  val dbUserApp = new DbUserApp {}
  val dbUserEmail = new DbUserEmail {}
  val dbUserKaredos = new DbUserKaredos {}
  val dbUserMobile = new DbUserMobile {}
  val dbUserPrefs = new DbUserPrefs {}
  val dbUserProfile = new DbUserProfile {}
  val dbUserSession = new DbUserSession {}
  val dbUserMessages = new DbUserMessages {}
  val dbUserIntent = new DbUserIntent {}
  val dbSale = new DbSale {}
  val dbMobileSale = new DbMobileSale {}
  val dbEmailVerify = new DbEmailVerify {}
  val dbUserInteract = new DbUserInteract {}
  val dbUserShare = new DbUserShare {}
  val dbUserFavourite = new DbUserFavourite {}
  val dbUrlMagic = new DbUrlMagic {}
  val dbUserUrlAccess = new DbUserUrlAccess {}
  val dbHashedAccount = new DbHashedAccount {}
  val dbInform = new DbInform {}
  val dbAPIMessages = new DbAPIMessages {}
}
