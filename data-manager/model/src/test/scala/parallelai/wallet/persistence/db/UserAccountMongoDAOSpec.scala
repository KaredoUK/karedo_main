package parallelai.wallet.persistence.db

import java.util.UUID

import org.specs2.matcher.{MatchResult, TryMatchers}
import org.specs2.mutable.Specification
import parallelai.wallet.persistence.mongodb.MongoTestUtils

class UserAccountMongoDAOSpec
  extends Specification
    with TryMatchers
    with MongoTestUtils {

  val accountDAO = new DbUserAccount {}

  val userAccount = UserAccount()
  val filledUserAccount = UserAccount(
    password=Some("hashed"),
    mobile=List(Mobile(msisdn = "12345678"),Mobile(msisdn="44444")),
    email=List(Email(address = "pakkio@gmail.com"),Email("daisy@gmail.com")))


  sequential



  accountDAO.deleteAll()


  "UserAccountMongoDAO" should {

    "Save and retrieve a user account" in {
      checkInsert(userAccount)

      // must fail if using the same id twice
      accountDAO.insertNew(userAccount.id,userAccount) must beFailedTry

    }
    "Save and retrieve a user account with data" in {
      checkInsert(filledUserAccount)
    }
    "should give error with invalid UUID" in {
      accountDAO.getById(UUID.randomUUID()) shouldEqual None
    }
    "should update the data for this user" in {
      accountDAO.getById(userAccount.id) match {
        case Some(account) =>
          val updated = account.copy(
            mobile=List(Mobile("12345678")),
            email=List(Email("pluto@gmail.com")),
            password=Some("Hash2"))

          accountDAO.update(updated.id,updated) must beSuccessfulTry
          accountDAO.getById(account.id) must beEqualTo(Some(updated))
        case _ => fail()
      }
    }
    "should be able to delete the user if needed" in {
      accountDAO.getById(userAccount.id) match {
        case Some(account) =>
          accountDAO.delete(account.id,account) must beSuccessfulTry
          accountDAO.getById(account.id) must beEqualTo(None)
        case _ => fail()
      }
    }
  }
  def fail(): MatchResult[Any] = 1===0

  def checkInsert(ua:UserAccount): MatchResult[Any] = {
    accountDAO.insertNew(ua.id,ua).get must beEqualTo(Some(ua.id))
    accountDAO.getById(ua.id) must beEqualTo(Some(ua))
  }
}
