package karedo.entity

import java.util.UUID

import karedo.entity.dao.{KO, OK}
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
      accountDAO.insertNew(userAccount) must beKO

    }
    "Save and retrieve a user account with data" in {
      checkInsert(filledUserAccount)
    }
    "should give error with invalid UUID" in {
      accountDAO.getById(UUID.randomUUID()) must beKO
    }
    "should update the data for this user" in {
      accountDAO.getById(userAccount.id) match {
        case OK(account) =>
          val updated = account.copy(
            mobile=List(Mobile(msisdn = "12345678")),
            email=List(Email(address = "pluto@gmail.com")),
            password=Some("Hash2")
            )

          accountDAO.update(updated.id,updated) must beOK
          accountDAO.getById(account.id) match {
            case OK(x) =>
              x.id must beEqualTo(userAccount.id)
              x.email(0).address must beEqualTo(updated.email(0).address)
              x.password must beEqualTo(updated.password)
              x.info must beEqualTo(userAccount.info)
            case KO(err) => ko(err)
          }

        case KO(err) => ko(err)
      }
    }
    "should be able to delete the user if needed" in {
      val tobedeleted = UserAccount()
      checkInsert(tobedeleted)
      accountDAO.getById(tobedeleted.id) match {
        case OK(account) =>
          accountDAO.delete(account.id,account) must beOK
          accountDAO.getById(account.id) must beKO
        case KO(err) => ko(err)
      }
    }
  }


  def checkInsert(ua:UserAccount): MatchResult[Any] = {
    accountDAO.insertNew(ua) must beOK
    accountDAO.getById(ua.id) must beOK

  }
}
