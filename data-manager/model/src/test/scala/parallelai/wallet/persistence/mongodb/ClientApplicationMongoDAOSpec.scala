package parallelai.wallet.persistence.mongodb

import org.specs2.mutable.Specification
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.time.NoTimeConversions
import com.escalatesoft.subcut.inject.NewBindingModule._
import scala.concurrent.ExecutionContext.Implicits.global
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import java.util.UUID

class ClientApplicationMongoDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions with MongoTestUtils {
  sequential

  "ClientApplicationMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> s"$embedConnectionPort",
        "mongo.db.name" -> "test",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )

    val accountDAO = new UserAccountMongoDAO
    val clientAppDAO = new ClientApplicationMongoDAO

    val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), Some("user@email.com"), totalPoints = 10l)
    val clientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")


    "Find a user's application" in {
      accountDAO.insertNew(userAccount, clientApplication)
      val findAfterInsert = clientAppDAO.getById(clientApplication.id)

      findAfterInsert shouldEqual Some(clientApplication)
    }

    "Find a user by app id" in {
      accountDAO.insertNew(userAccount, clientApplication)
      val findUserByAppId = accountDAO.getByApplicationId(clientApplication.id)

      findUserByAppId shouldEqual Some(userAccount)
    }

    "Update an application" in {
      val updated = clientApplication.copy(activationCode = "new activation code", active = true)
      accountDAO.insertNew(userAccount, clientApplication)
      clientAppDAO.update(updated)
      val findAfterUpdate = clientAppDAO.getById(clientApplication.id)

      findAfterUpdate shouldEqual Some(updated)
    }

    "Add a new application to an existing user, shoud be in the DB" in {
      val secondClientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

      accountDAO.insertNew(userAccount, clientApplication)
      clientAppDAO.insertNew(secondClientApplication)
      val findAfterAddingToUser = clientAppDAO.getById(secondClientApplication.id)

      findAfterAddingToUser shouldEqual Some(secondClientApplication)
    }

    "Load an account with two apps" in {
      val secondClientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

      accountDAO.insertNew(userAccount, clientApplication, secondClientApplication)
      accountDAO.getById(userAccount.id)
      val findAfterAddingToUser = clientAppDAO.getById(secondClientApplication.id)

      findAfterAddingToUser shouldEqual Some(secondClientApplication)
    }

    "Add a new application to an existing user, should be associeted to the user" in {
      val secondClientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

      accountDAO.insertNew(userAccount, clientApplication)
      clientAppDAO.insertNew(secondClientApplication)
      val findUserAfterAddingNewApp = accountDAO.getByApplicationId(secondClientApplication.id)

      findUserAfterAddingNewApp shouldEqual Some(userAccount)
    }

  }
}
