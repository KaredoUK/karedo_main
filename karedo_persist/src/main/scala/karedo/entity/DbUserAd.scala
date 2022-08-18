package karedo.entity

import karedo.entity.dao.{DbDao, DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._

case class Channel
(
  channel: String
  , channel_id: String
)

case class UserAd
(
  // this is the accountId
  @Key("_id") id: String
  , application_id: String
  , adType: String
  , ad_id: Int
  , imp_url: String
  , click_url: String
  , adddomain: String
  , cid: String
  , crid: String
  , channels: List[Channel]
  , ts: DateTime = DbDao.now

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbUserAd extends DbMongoDAO[String,UserApp] {

}



