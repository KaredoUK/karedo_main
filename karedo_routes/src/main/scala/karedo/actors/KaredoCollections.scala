package karedo.actors

import karedo.entity.{DbUserAccount, DbUserAd, DbUserApp, DbUserSession}

/**
  * Created by pakkio on 10/8/16.
  */
trait KaredoCollections {
  val dbUserApp = new DbUserApp {}
  val dbUserAccount = new DbUserAccount {}
  val dbUserSession = new DbUserSession {}
  val dbUserAd = new DbUserAd {}
}
