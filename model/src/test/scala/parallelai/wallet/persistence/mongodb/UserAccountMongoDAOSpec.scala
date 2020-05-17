package parallelai.wallet.persistence.mongodb

import org.specs2.mutable.Specification
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import com.escalatesoft.subcut.inject.NewBindingModule
import NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigMapSource
import parallelai.wallet.entity.{ClientApplication, UserPersonalInfo, UserAccount}
import java.util.UUID
import scala.concurrent.{Future, Await}
import Await._
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserAccountMongoDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions with MongoTestUtils {

  sequential

  "UserAccountMongoDAO" should {

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

    val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), Some("user@email.com"))
    val clientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")
    val activeAccount = UserAccount(UUID.randomUUID(), Some("87654321"), Some("other.user@email.com"), active = true)
    val activeClientApplication = ClientApplication(UUID.randomUUID(), activeAccount.id, "ACT_CODE_1", active = true)

    "Save and retreive a user account" in {
      val findAfterInsert =
           for {
             insert <- accountDAO.insertNew(userAccount, clientApplication)
             read <- accountDAO.getById(userAccount.id)
           } yield read


      fromFuture(findAfterInsert) shouldEqual Some(userAccount)

    }


    "Find account by application ID" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          read <- accountDAO.getByApplicationId(clientApplication.id)
        } yield read

      fromFuture(findAfterInsert) shouldEqual Some(userAccount)
    }

    "Don't find anything with wrong ID" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          read <- accountDAO.getById(UUID.randomUUID())
        } yield read


      fromFuture(findAfterInsert) shouldEqual None
    }

    "Not Find account using wrong application ID" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          read <- accountDAO.getByApplicationId(UUID.randomUUID())
        } yield read

      fromFuture(findAfterInsert) shouldEqual None
    }

    "Find by email" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          read <- accountDAO.getByEmail(userAccount.email.get)
        } yield read

      fromFuture(findAfterInsert) shouldEqual Some(userAccount)
    }

    "Find by email filtering with active status" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          insert1 <- accountDAO.insertNew(activeAccount, activeClientApplication)

          inactive <- accountDAO.getByEmail(userAccount.email.get, true)
          active <- accountDAO.getByEmail(activeAccount.email.get, true)
        } yield (inactive, active)

      fromFuture(findAfterInsert) shouldEqual (None, Some(activeAccount))
    }

    "Find by application_id filtering with active status" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          insert1 <- accountDAO.insertNew(activeAccount, activeClientApplication)

          inactive <- accountDAO.getByApplicationId(clientApplication.id, true)
          active <- accountDAO.getByApplicationId(activeClientApplication.id, true)
        } yield (inactive, active)

      fromFuture(findAfterInsert) shouldEqual (None, Some(activeAccount))
    }

    "Find by any of id, email, application id" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)

          byMsisdn <- accountDAO.findByAnyOf(Some(UUID.randomUUID()), userAccount.msisdn, None)
          byEmail <- accountDAO.findByAnyOf(None, Some("-----"), userAccount.email)
          byApplicationId <- accountDAO.findByAnyOf(Some(clientApplication.id), Some("____"), Some("____"))
          shouldntFind <- accountDAO.findByAnyOf(Some(UUID.randomUUID()), Some("-----"), Some("notanemail"))
        } yield (byMsisdn, byEmail, byApplicationId, shouldntFind)

      fromFuture(findAfterInsert) shouldEqual (Some(userAccount), Some(userAccount), Some(userAccount), None)
    }

    "Find by any of id, email, application id, passing only one param" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)

          byMsisdn <- accountDAO.findByAnyOf(None, userAccount.msisdn, None)
          byEmail <- accountDAO.findByAnyOf(None, None, userAccount.email)
          byApplicationId <- accountDAO.findByAnyOf(Some(clientApplication.id), None, None)
          shouldntFind <- accountDAO.findByAnyOf(None, None, None)
        } yield (byMsisdn, byEmail, byApplicationId, shouldntFind)

      fromFuture(findAfterInsert) shouldEqual (Some(userAccount), Some(userAccount), Some(userAccount), None)
    }

    "Delete by ID" in {
      val findAfterDelete = for {
        insert <- accountDAO.insertNew(userAccount, clientApplication)
        delete <- accountDAO.delete(userAccount.id)
        read <- accountDAO.getById(userAccount.id)
      } yield read

      fromFuture(findAfterDelete) shouldEqual None
    }

    "Set account active" in {
      val findAfterSettingActive = for {
        insert <- accountDAO.insertNew(userAccount, clientApplication)
        active <- accountDAO.setActive(userAccount.id)
        read <- accountDAO.getById(userAccount.id)
      } yield read

      fromFuture(findAfterSettingActive) map { _.active } shouldEqual Some(true)
    }

  }

}
