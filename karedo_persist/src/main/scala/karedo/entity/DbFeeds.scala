package karedo.entity

import karedo.entity.dao.{DbMongoDAO_Casbah, Keyable}
import karedo.util.Util
import salat.annotations.Key

/**
  * Created by charaj on 07/02/2017.
  */

case class Feed
(
  @Key("_id") id: String = Util.newUUID
  , name: String
  , url: String
  , fallback_img: String
  , enabled: Boolean = true
  , locale: Option[String] = None
  , prefs: List[String] = List() // Maps to IABs prefs in DbPrefs

) extends Keyable[String]

trait DbFeeds extends DbMongoDAO_Casbah[String, Feed]
