package karedo.actors

import karedo.entity._

/**
  * Created by pakkio on 10/8/16.
  */
trait KaredoCollections {
  val dbUserApp = new DbUserApp {}
  val dbUserAccount = new DbUserAccount {}
  val dbUserSession = new DbUserSession {}
  val dbUserAd = new DbUserAd {}
  val dbKaredoChange = new DbKaredoChange {}
  val dbUserKaredos = new DbUserKaredos {}
  val dbUserMessages = new DbUserMessages {}
  val dbAds = new DbAds {}
}
