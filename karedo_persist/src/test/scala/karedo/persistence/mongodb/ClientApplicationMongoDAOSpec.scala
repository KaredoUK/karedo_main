package karedo.persistence.mongodb

import java.util.UUID

class ClientApplicationMongoDAOSpec
  extends Specification
  with MongoTestUtils
  with BeforeExample
{

  val accountDAO = new UserAccountMongoDAO
  val clientAppDAO = new ClientApplicationMongoDAO

  def before = accountDAO.dao.collection.remove(MongoDBObject())

  sequential

  "ClientApplicationMongoDAO" should {



    val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), Some("user@email.com"), totalPoints = 10)
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
