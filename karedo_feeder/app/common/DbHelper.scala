package common

import karedo.persist.entity.{DbAds, DbFeeds, DbPrefs}

/**
  * Created by charaj on 11/02/2017.
  */
trait DbHelper {
  val dbFeeds = new DbFeeds {}
  val dbPrefs = new DbPrefs {}
  val dbAds = new DbAds {}
}
