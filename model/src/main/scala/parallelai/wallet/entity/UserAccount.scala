package parallelai.wallet.entity

import java.util.UUID
import org.joda.time.DateTime

case class ClientApplication(id: UUID, activationCode: String, active: Boolean, accountId: UUID)
case class UserInfo(name: String, postCode: Option[String] = None, birthDate: Option[DateTime] = None, gender: Option[String] = None)
case class AccountSettings(maxMessagesPerWeek: Int)
case class UserAccount(id: UUID, msisdn: Option[String], email: Option[String], personalInfo: UserInfo = defaultUserInfo, settings: AccountSettings = defaultAccountSettings)


