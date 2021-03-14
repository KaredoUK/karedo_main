package parallelai.wallet.persistence.mongodb

import java.util.UUID
import parallelai.wallet.entity.KaredoTypes.KaredoPoints
import parallelai.wallet.entity._
import com.novus.salat.annotations._
import org.joda.time.DateTime

object MongoUserApplicationInfo {
  def fromClientApplication(clientApp: ClientApplication): MongoUserApplicationInfo =
    MongoUserApplicationInfo(clientApp.id, clientApp.activationCode, clientApp.active)
}

//case class SubscribedBrand(brandId:UUID, lastAction:DateTime)

case class MongoUserApplicationInfo(@Key("_id")id: UUID, activationCode: String, active: Boolean = false) {
  def toClientApplication(accountId: UUID) = ClientApplication(id, accountId, activationCode, active)
}
case class MongoUserAccount(
                             @Key("_id") id: UUID,
                             msisdn: Option[String],
                             email: Option[String],
                             userType: String="CUSTOMER",
                             personalInfo: UserPersonalInfo = defaultUserPersonalInfo,
                             settings: AccountSettings = defaultAccountSettings,
                             password: Option[String] = None,
                             active: Boolean = false,
                             totalPoints: KaredoPoints = 0,
                             subscribedBrands: List[SubscribedBrand] = List[SubscribedBrand](),
                             applications: List[MongoUserApplicationInfo] = List[MongoUserApplicationInfo]()
                             ) {
  def toUserAccount : UserAccount = 
    UserAccount(id, msisdn, email, userType, personalInfo, settings, active, totalPoints, 
        subscribedBrands, password)

  def toClientApplicationList : List[ClientApplication] = applications map { _.toClientApplication(id)  }
}
