import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import org.specs2.mutable.Specification


class Testing
  extends Specification
  with RegistrationHelpers
  with MyUtility {


  "Testing all data-server-api" should {
    "UserRegistrationBlock" in {

      val (applicationId, pass, userId, sessionId) = RegisterAccount

      isUUID(applicationId.toString)
      isUUID(userId.toString)
      isUUID(sessionId)



    }
    "Reset application" in {
      val (applicationId, activationCode0, activationCode) = ResetAccount

      activationCode0 must be_!==(activationCode)
    }
  }

}
