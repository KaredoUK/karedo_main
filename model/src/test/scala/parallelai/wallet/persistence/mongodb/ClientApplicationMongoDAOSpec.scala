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
      val findAfterInsert = for {
          createNew <- accountDAO.insertNew(userAccount, clientApplication)
          getAppById <- clientAppDAO.getById(clientApplication.id)
        } yield getAppById

      fromFuture(findAfterInsert) shouldEqual Some(clientApplication)
    }

    "Find a user by app id" in {
      val findUserByAppId = for {
        createNew <- accountDAO.insertNew(userAccount, clientApplication)
        findUserByAppId <- accountDAO.getByApplicationId(clientApplication.id)
      } yield findUserByAppId

      fromFuture(findUserByAppId) shouldEqual Some(userAccount)
    }

    "Update an application" in {
      val updated = clientApplication.copy(activationCode = "new activation code", active = true)
      val findAfterUpdate = for {
        createNew <- accountDAO.insertNew(userAccount, clientApplication)
        update <- clientAppDAO.update(updated)
        updated <- clientAppDAO.getById(clientApplication.id)
      } yield updated

      fromFuture(findAfterUpdate) shouldEqual Some(updated)
    }

    "Add a new application to an existing user, shoud be in the DB" in {
      val secondClientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

      val findAfterAddingToUser = for {
        createNew <- accountDAO.insertNew(userAccount, clientApplication)
        update <- clientAppDAO.insertNew(secondClientApplication)
        inserted <- clientAppDAO.getById(secondClientApplication.id)
      } yield inserted

      fromFuture(findAfterAddingToUser) shouldEqual Some(secondClientApplication)
      None shouldEqual None
    }

    "Load an account with two apps" in {
      val secondClientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

      val findAfterAddingToUser = for {
        createNew <- accountDAO.insertNew(userAccount, clientApplication, secondClientApplication)
        user <- accountDAO.getById(userAccount.id)
        inserted <- clientAppDAO.getById(secondClientApplication.id)
      } yield inserted

      fromFuture(findAfterAddingToUser) shouldEqual Some(secondClientApplication)
    }

    "Add a new application to an existing user, should be associeted to the user" in {
      val secondClientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

      val findUserAfterAddingNewApp = for {
        createNew <- accountDAO.insertNew(userAccount, clientApplication)
        update <- clientAppDAO.insertNew(secondClientApplication)
        findUserByAppId <- accountDAO.getByApplicationId(secondClientApplication.id)
      } yield findUserByAppId

      fromFuture(findUserAfterAddingNewApp) shouldEqual Some(userAccount)
    }

  }
}
