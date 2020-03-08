package parallelai.wallet.entity

import java.util.UUID
import org.joda.time.DateTime

case class ClientApplication(id: UUID, activationCode: String, active: Boolean)
case class UserInfo(name: String, address: String, birthDate: DateTime)
case class AccountSettings(maxMessagesPerDay: Int)
case class UserAccount(
                        id: UUID, 
                        registeredApplications: Seq[ClientApplication],
                        personalInfo: UserInfo, 
                        settings: AccountSettings
                        ) 
