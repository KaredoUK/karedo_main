package parallelai.wallet.persistence.mongodb

import org.specs2.mutable.{Before, BeforeAfter, After, Specification}
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import com.escalatesoft.subcut.inject.NewBindingModule
import NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigMapSource
import org.specs2.specification.BeforeExample
import parallelai.wallet.entity.{ClientApplication, UserPersonalInfo, UserAccount}
import java.util.UUID
import scala.concurrent.{Future, Await}
import Await._
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserAccountMongoDAOSpec
  extends Specification
  with TestWithLocalMongoDb
  with BeforeExample
{
  sequential

  def before = clearAll()

  "UserAccountMongoDAO" should {

    "Save and retrieve a user account" in {
      accountDAO.insertNew(userAccount, clientApplication)
      val findAfterInsert = accountDAO.getById(userAccount.id)

      findAfterInsert shouldEqual Some(userAccount)
    }


    "Find account by application ID" in {

      accountDAO.insertNew(userAccount, clientApplication)
      val findAfterInsert =accountDAO.getByApplicationId(clientApplication.id)

      findAfterInsert shouldEqual Some(userAccount)
    }

    "Don't find anything with wrong ID" in {
      clearAll()
      accountDAO.insertNew(userAccount, clientApplication)

      accountDAO.getById(UUID.randomUUID()) shouldEqual None
    }

    "Not Find account using wrong application ID" in {
      accountDAO.insertNew(userAccount, clientApplication)

      accountDAO.getByApplicationId(UUID.randomUUID()) shouldEqual None
    }

    "Find by email" in {
      accountDAO.insertNew(userAccount, clientApplication)
      val findAfterInsert = accountDAO.getByEmail(userAccount.email.get)

      findAfterInsert shouldEqual Some(userAccount)
    }

    "Find by email filtering with active status" in {

      accountDAO.insertNew(userAccount, clientApplication)
      accountDAO.insertNew(activeAccount, activeClientApplication)

      val inactive = accountDAO.getByEmail(userAccount.email.get, true)
      val active = accountDAO.getByEmail(activeAccount.email.get, true)


      inactive shouldEqual None
      active shouldEqual Some(activeAccount)
    }

    "Find by application_id filtering with active status" in {
      accountDAO.insertNew(userAccount, clientApplication)
      accountDAO.insertNew(activeAccount, activeClientApplication)
      accountDAO.setPassword(activeAccount.id,"pass")

      val inactive = accountDAO.getByApplicationId(clientApplication.id, true)
      val active = accountDAO.getByApplicationId(activeClientApplication.id, true)
      val checkPassword = accountDAO.checkPassword(activeAccount.id,"pass")
      val wrongPassword = accountDAO.checkPassword(activeAccount.id,"pass2")

      inactive shouldEqual None
      val activeAddingPassword=activeAccount.copy(password=Some("pass"))
      active shouldEqual Some(activeAddingPassword)

      checkPassword should beTrue

      wrongPassword should beFalse

      // this previously failed
      active.get.password should beEqualTo(Some("pass"))
    }

    "Find by any of id, email, application id" in {
      accountDAO.insertNew(userAccount, clientApplication)

      val byMsisdn = accountDAO.findByAnyOf(Some(UUID.randomUUID()), userAccount.msisdn, None)
      val byEmail = accountDAO.findByAnyOf(None, Some("-----"), userAccount.email)
      val byApplicationId = accountDAO.findByAnyOf(Some(clientApplication.id), Some("____"), Some("____"))
      val shouldntFind = accountDAO.findByAnyOf(Some(UUID.randomUUID()), Some("-----"), Some("notanemail"))

      byMsisdn shouldEqual Some(userAccount)
      byEmail shouldEqual Some(userAccount)
      byApplicationId shouldEqual Some(userAccount)
      shouldntFind shouldEqual None
    }

    "Find by any of id, email, application id, passing only one param" in {
      accountDAO.insertNew(userAccount, clientApplication)

      val byMsisdn = accountDAO.findByAnyOf(None, userAccount.msisdn, None)
      val byEmail = accountDAO.findByAnyOf(None, None, userAccount.email)
      val byApplicationId = accountDAO.findByAnyOf(Some(clientApplication.id), None, None)
      val shouldntFind = accountDAO.findByAnyOf(None, None, None)

      byMsisdn shouldEqual Some(userAccount)
      byEmail shouldEqual Some(userAccount)
      byApplicationId shouldEqual Some(userAccount)
      shouldntFind shouldEqual None
    }

    "Delete by ID" in {
      accountDAO.insertNew(userAccount, clientApplication)
      accountDAO.delete(userAccount.id)

      accountDAO.getById(userAccount.id) shouldEqual None
    }

    "Set account active" in {
      accountDAO.insertNew(userAccount, clientApplication)
      accountDAO.setActive(userAccount.id)

      val activeStatus = accountDAO.getById(userAccount.id) map { _.active }

      activeStatus shouldEqual Some(true)
    }

  }
}
