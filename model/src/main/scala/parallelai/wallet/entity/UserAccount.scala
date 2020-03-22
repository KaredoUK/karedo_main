package parallelai.wallet.entity

import java.util.UUID
import org.joda.time.DateTime

case class EmailUserLookup(email: String, userId: UUID)
case class MsisdnUserLookup(msisdn: String, userId: UUID)
case class ClientApplication(id: UUID, accountId: UUID, activationCode: String, active: Boolean = false)
case class UserInfo(name: String, postCode: Option[String] = None, birthDate: Option[DateTime] = None, gender: Option[String] = None)
case class AccountSettings(maxMessagesPerWeek: Int)
case class UserAccount(id: UUID, msisdn: Option[String], email: Option[String],
                       personalInfo: UserInfo = defaultUserInfo, settings: AccountSettings = defaultAccountSettings,
                       active: Boolean = false)


