package parallelai.wallet.entity

import java.util.UUID
import org.joda.time.DateTime

case class EmailUserLookup(email: String, userId: UUID)
case class MsisdnUserLookup(msisdn: String, userId: UUID)
case class ClientApplication(id: UUID, accountId: UUID, activationCode: String, active: Boolean = false)
case class UserPersonalInfo(name: String, postCode: Option[String] = None, birthDate: Option[DateTime] = None, gender: Option[String] = None)
case class AccountSettings(maxMessagesPerWeek: Int)
case class UserAccount(id: UUID, msisdn: Option[String], email: Option[String],
                       personalInfo: UserPersonalInfo = defaultUserPersonalInfo,
                       settings: AccountSettings = defaultAccountSettings,
                       active: Boolean = false,
                       totalPoints: Long = 0)

case class UserAds(userId: UUID, readAds: Set[UUID])
case class UserRewards(userId: UUID, rewards: Set[UUID])

case class Reward(id: UUID, brandId: UUID, description: String, image: Array[Byte], qrCode: Array[Byte], value: Long)

case class AdvertisementMetadata(detailId: UUID, publishedDate: DateTime)
case class AdvertisementDetail(id: UUID, text: String, image: Array[Byte], value: Int)
case class Store(id: UUID, name: String, icon: Array[Byte], ads: List[AdvertisementMetadata])