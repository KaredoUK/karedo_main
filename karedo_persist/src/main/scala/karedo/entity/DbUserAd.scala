package karedo.entity

import com.mongodb.casbah.commons.MongoDBObject
import karedo.entity.dao.{DbMongoDAO, Keyable}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

case class Channel
(
  channel: String
  , channel_id: String
)

case class UserAd
(
  // this is the univoque AdId
  @Key("_id") id: String
  , account_id: String
  , adType: String = ""
  , imp_url: String
  , click_url: String
  , addomain: String
  , cid: String
  , crid: String
  , channels: List[Channel] = List()
  , ts: DateTime = now

) extends Keyable[String]

// add implementation if you need special functionalities
trait DbUserAd extends DbMongoDAO[String, UserAd] {
  // preloads some values associated to accountId: accountid
  def preload() = {

    def add(index: Int) = {
      val applId = "applId"
      val accountId = "acctId"
      val inserted = insertNew(UserAd(id = index.toString, accountId, applId, imp_url = s"http://www.ad.com/?id=$index",
        addomain = "domain.com", click_url = "http://www.ad.com/click/?c=45", cid = "cid", crid = "crid"
        ,channels = List(Channel("channel","CH01"), Channel("channel2","CH02"))
      ))
    }

    deleteAll()
    for (i <- 1 to 100) add(i)
    100

  }

  def getAdsForUser(accountId: String) = {
    dao.find(MongoDBObject("account_id" -> accountId)).sort(orderBy = MongoDBObject("sort" -> 1)).toList
  }
}



