package parallelai.wallet.entity

import java.util.UUID
import com.novus.salat.annotations.Key
import org.bson.types.ObjectId
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
                       totalPoints: Long = 0,
                       subscribedBrands: List[UUID] = List())

case class UserAds(userId: UUID, readAds: Set[UUID])
case class UserOffers(userId: UUID, rewards: Set[UUID])

case class Offer(@Key("_id") id: UUID = UUID.randomUUID(), name: String = "", brandId: UUID, description: String, imagePath: String, qrCodeId: UUID, value: Long)

case class AdvertisementMetadata(detailId: UUID, publishedDate: DateTime)
case class AdvertisementDetail(@Key("_id") id: UUID = UUID.randomUUID(), text: String, imageIds: List[UUID], value: Int)
case class Brand(@Key("_id") id: UUID = UUID.randomUUID(), name: String = "", iconId: UUID, ads: List[AdvertisementMetadata]=List())