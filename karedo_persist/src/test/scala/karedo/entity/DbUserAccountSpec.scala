package karedo.entity

import java.util.UUID

import org.specs2.matcher.{EitherMatchers, MatchResult, TryMatchers}
import org.specs2.mutable.Specification
import utils.MongoTestUtils

class DbUserAccountSpec
  extends Specification
    with EitherMatchers
    with MongoTestUtils {

  val accountDAO = new DbUserAccount {}

  val userAccount = UserAccount(info=Some("just for test"))
  val filledUserAccount = UserAccount(

    password=Some("hashed"),
    mobile=List(Mobile(msisdn = "12345678"),Mobile(msisdn="44444")),
    email=List(Email(address = "pakkio@gmail.com"),Email( address = "daisy@gmail.com")))


  sequential



  accountDAO.deleteAll()


  "UserAccountMongoDAO" should {

    "Save and retrieve a user account" in {
      checkInsert(userAccount)

      // must fail if using the same id twice
      accountDAO.insertNew(userAccount.id,userAccount) must beLeft

    }
    "Save and retrieve a user account with data" in {
      checkInsert(filledUserAccount)
    }
    "should give error with invalid UUID" in {
      accountDAO.getById(UUID.randomUUID()) must beLeft
    }
    "should update the data for this user" in {
      accountDAO.getById(userAccount.id) match {
        case Right(account) =>
          val updated = account.copy(
            mobile=List(Mobile(msisdn = "12345678")),
            email=List(Email(address = "pluto@gmail.com")),
            password=Some("Hash2")
            )

          accountDAO.update(updated.id,updated) must beRight
          accountDAO.getById(account.id) match {
            case Right(x) =>
              x.id must beEqualTo(userAccount.id)
              x.email(0).address must beEqualTo(updated.email(0).address)
              x.password must beEqualTo(updated.password)
              x.info must beEqualTo(userAccount.info)
            case _ => fail()
          }

        case _ => fail()
      }
    }
    "should be able to delete the user if needed" in {
      val tobedeleted = UserAccount()
      checkInsert(tobedeleted)
      accountDAO.getById(tobedeleted.id) match {
        case Right(account) =>
          accountDAO.delete(account.id,account) must beRight
          accountDAO.getById(account.id) must beLeft
        case _ => fail()
      }
    }
  }
  def fail(): MatchResult[Any] = 1===0

  def checkInsert(ua:UserAccount): MatchResult[Any] = {
    accountDAO.insertNew(ua.id,ua) must beRight
    accountDAO.getById(ua.id) must beRight

  }
}
