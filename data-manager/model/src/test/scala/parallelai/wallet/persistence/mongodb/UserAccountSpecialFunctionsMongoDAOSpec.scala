package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeExample
import parallelai.wallet.entity.{ClientApplication, UserAccount}

class UserAccountSpecialFunctionsMongoDAOSpec
  extends Specification
  with MongoTestUtils {
  sequential


  val accountDAO = new UserAccountMongoDAO()

  accountDAO.dao.collection.remove(MongoDBObject())

  val clientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

  val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), Some("user@email.com"))

  "UserAccountMongoDAO" should {

    "Add points to account" in {
      val initialPoints = 200
      val increment = 5000

      val accountWithPoints = userAccount.copy(totalPoints = initialPoints)
      accountDAO.insertNew(accountWithPoints, clientApplication)
      val updated = accountDAO.addPoints(userAccount.id, increment)
      updated.get.totalPoints === (initialPoints + increment)


    }

  }

}
