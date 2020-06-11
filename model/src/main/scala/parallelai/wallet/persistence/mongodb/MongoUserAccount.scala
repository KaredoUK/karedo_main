package parallelai.wallet.persistence.mongodb

import java.util.UUID
import parallelai.wallet.entity._
import com.novus.salat.annotations._

case class MongoUserApplicationInfo(@Key("_id")id: UUID, activationCode: String, active: Boolean = false) {
  def toClientApplication(accountId: UUID) = ClientApplication(id, accountId, activationCode, active)
}
case class MongoUserAccount(
                @Key("_id") id: UUID,
                msisdn: Option[String],
                email: Option[String],
                password: Option[String],
                personalInfo: UserPersonalInfo = defaultUserPersonalInfo,
                settings: AccountSettings = defaultAccountSettings,
                active: Boolean = false,
                applications: List[MongoUserApplicationInfo]
  ) {
  def toUserAccount : UserAccount = UserAccount(id, msisdn, email, password, personalInfo, settings, active)

  def toClientApplicationList : List[ClientApplication] = applications map { _.toClientApplication(id)  }
}
