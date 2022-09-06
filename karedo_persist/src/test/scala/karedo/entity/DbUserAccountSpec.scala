package karedo.entity

import java.util.UUID

import karedo.util.{KO, OK}
import org.specs2.matcher.{EitherMatchers, MatchResult}
import org.specs2.mutable.Specification
import utils.MongoTestUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import karedo.util.Util.now


@RunWith(classOf[JUnitRunner])
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
      accountDAO.find(UUID.randomUUID()) must beKO
    }
    "should update the data for this user" in {
      accountDAO.find(userAccount.id) match {
        case OK(account) =>
          val updated = account.copy(
            mobile=List(Mobile(msisdn = "12345678")),
            email=List(Email(address = "pluto@gmail.com")),
            password=Some("Hash2")
            )

          accountDAO.update(updated) must beOK
          accountDAO.find(account.id) match {
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
    "should be able to find the active mobile and email" in {
      accountDAO.find(userAccount.id) match {
        case OK(account) => {
          accountDAO.update(account.copy(
            mobile = List(Mobile(msisdn = "1", valid = true, ts_validated = Some(now)), Mobile(msisdn = "2", valid = true, ts_validated = Some(now)), Mobile(msisdn = "3", valid = false)),
            email = List(Email(address = "1", valid = true, ts_validated = Some(now)), Email(address = "2", valid = true, ts_validated = Some(now)), Email(address = "3", valid = false))
          )) must beOK
          accountDAO.findActiveMobile(account.id) match {
            case OK(x) =>
              x.msisdn must beEqualTo("2")
            case KO(err) => ko(err)
          }
          accountDAO.findActiveEmail(account.id) match {
            case OK(x) =>
              x.address must beEqualTo("2")
            case KO(err) => ko(err)
          }
        }
        case KO(err) => ko(err)
      }

      accountDAO.find(userAccount.id) match {
        case OK(account) => {
          account.findActiveMobile match {
            case OK(x) =>
              x must not be null
              x.msisdn must beEqualTo("2")
            case KO(err) => ko(err)
          }
          account.findActiveEmail match {
            case OK(x) =>
              x.address must beEqualTo("2")
            case KO(err) => ko(err)
          }

        }
        case KO(err) => ko(err)
      }

      accountDAO.find(userAccount.id) match {
        case OK(account) => {
          accountDAO.update(account.copy(
            mobile = List(Mobile(msisdn = "4", valid = true, ts_validated = Some(now)), Mobile(msisdn = "5", valid = false)),
            email = List(Email(address = "4", valid = true, ts_validated = Some(now)), Email(address = "5", valid = false))
          )) must beOK
          accountDAO.findActiveMobile(account.id) match {
            case OK(x) =>
              x.msisdn must beEqualTo("4")
            case KO(err) => ko(err)
          }
          accountDAO.findActiveEmail(account.id) match {
            case OK(x) =>
              x.address must beEqualTo("4")
            case KO(err) => ko(err)
          }
        }
        case KO(err) => ko(err)
      }

      accountDAO.find(userAccount.id) match {
        case OK(account) => {
          account.findActiveMobile match {
            case OK(x) =>
              x.msisdn must beEqualTo("4")
            case KO(err) => ko(err)
          }
          account.findActiveEmail match {
            case OK(x) =>
              x.address must beEqualTo("4")
            case KO(err) => ko(err)
          }

        }
        case KO(err) => ko(err)
      }

      accountDAO.find(userAccount.id) match {
        case OK(account) => {
          accountDAO.update(account.copy(
            mobile = List(Mobile(msisdn = "6", valid = true, ts_validated = Some(now)), Mobile(msisdn = "7", valid = false, ts_validated = Some(now))),
            email = List(Email(address = "6", valid = true, ts_validated = Some(now)), Email(address = "7", valid = false, ts_validated = Some(now)))
          )) must beOK
          accountDAO.findActiveMobile(account.id) match {
            case OK(x) =>
              x.msisdn must beEqualTo("6")
            case KO(err) => ko(err)
          }
          accountDAO.findActiveEmail(account.id) match {
            case OK(x) =>
              x.address must beEqualTo("6")
            case KO(err) => ko(err)
          }
        }
        case KO(err) => ko(err)
      }

      accountDAO.find(userAccount.id) match {
        case OK(account) => {
          account.findActiveMobile match {
            case OK(x) =>
              x.msisdn must beEqualTo("6")
            case KO(err) => ko(err)
          }
          account.findActiveEmail match {
            case OK(x) =>
              x.address must beEqualTo("6")
            case KO(err) => ko(err)
          }

        }
        case KO(err) => ko(err)
      }

    }
    "should be able to delete the user if needed" in {
      val tobedeleted = UserAccount()
      checkInsert(tobedeleted)
      accountDAO.find(tobedeleted.id) match {
        case OK(account) =>
          accountDAO.delete(account) must beOK
          accountDAO.find(account.id) must beKO
        case KO(err) => ko(err)
      }
    }
  }


  def checkInsert(ua:UserAccount): MatchResult[Any] = {
    accountDAO.insertNew(ua) must beOK
    accountDAO.find(ua.id) must beOK

  }
}
