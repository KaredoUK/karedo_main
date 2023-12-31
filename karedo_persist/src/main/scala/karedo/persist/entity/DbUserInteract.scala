package karedo.persist.entity

import karedo.common.misc.Util
import karedo.common.misc.Util.now
import karedo.persist.entity.dao._
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

trait DbUserInteract extends DbMongoDAO_Casbah[String, UserInteraction]

trait DbUserShare extends DbMongoDAO_Casbah[String, UserInteraction]

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

trait DbUserFavourite extends DbMongoDAO_Casbah[String, UserFavourite]

case class UrlMagic
(
  @Key("_id") id:String
  , first_url: String
  , second_url: Option[String]
  , ts:DateTime = now
) extends Keyable[String]

trait DbUrlMagic extends DbMongoDAO_Casbah[String, UrlMagic]

case class HashedAccount
(
  @Key("_id") id:String = Util.newUUID
  , account_id: String
) extends Keyable[String]

trait DbHashedAccount extends DbMongoDAO_Casbah[String, HashedAccount]

case class UrlAccess
(
  @Key("_id") id:String = Util.newUUID
  , account_id: String
  , access_url: String
  , ts:DateTime = now
) extends Keyable[String]

trait DbUserUrlAccess extends DbMongoDAO_Casbah[String, UrlAccess]

