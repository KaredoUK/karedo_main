package karedo.entity

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
}
