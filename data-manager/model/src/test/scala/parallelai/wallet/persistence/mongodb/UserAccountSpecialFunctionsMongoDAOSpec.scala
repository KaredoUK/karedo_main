package parallelai.wallet.persistence.mongodb

import java.util.UUID

import org.specs2.mutable.Specification
import org.specs2.specification.BeforeExample

class UserAccountSpecialFunctionsMongoDAOSpec
  extends Specification
  with TestWithLocalMongoDb
  with BeforeExample
{
  sequential

  def before = clearAll()

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
