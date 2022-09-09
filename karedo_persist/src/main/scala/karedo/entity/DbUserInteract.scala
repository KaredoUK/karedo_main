package karedo.entity

import karedo.entity.dao.{DbMongoDAO, Keyable}
import karedo.util.Util
import karedo.util.Util.now
import org.joda.time.DateTime
import salat.annotations._

/**
  * Created by crajah on 14/10/2016.
  */
case class UserInteraction
(
    @Key("_id") id:String = Util.newUUID
  , account_id: String
  , interaction: InteractUnit
  , ts: DateTime = now
) extends Keyable[String]

case class InteractUnit
(
    action_type: Option[String] = None
  , ad_type: String
  , ad_id: String
  , ad_text: Option[String] = None
  , imp_url: String
  , click_url: String
  , ad_domain: String
  , cid: String
  , crid: String
  , channels: Option[List[ChannelUnit]] = None
)

case class ChannelUnit
(
   channel: String
  , channel_id: String
  , share_data: Option[String] = None
  , share_url: Option[String] = None
)

trait DbUserInteract extends DbMongoDAO[String, UserInteraction]

trait DbUserShare extends DbMongoDAO[String, UserInteraction]

case class UserFavourite
(
  @Key("_id") id:String
  , entries: List[FavouriteUnit]
) extends Keyable[String]


case class FavouriteUnit
(
    ad_id: String
  , ad_domain: String
  , cid: String
  , crid: String
  , favourite: Option[Boolean] = Some(true)
  , ts: Option[DateTime] = Some(now)
)

trait DbUserFavourite extends DbMongoDAO[String, UserFavourite]

case class UrlMagic
(
  @Key("_id") id:String
  , first_url: String
  , second_url: Option[String]
  , ts:DateTime = now
) extends Keyable[String]

trait DbUrlMagic extends DbMongoDAO[String, UrlMagic]

case class HashedAccount
(
  @Key("_id") id:String = Util.newUUID
  , account_id: String
) extends Keyable[String]

trait DbHashedAccount extends DbMongoDAO[String, HashedAccount]

case class UrlAccess
(
  @Key("_id") id:String = Util.newUUID
  , account_id: String
  , access_url: String
  , ts:DateTime = now
) extends Keyable[String]

trait DbUserUrlAccess extends DbMongoDAO[String, UrlAccess]

