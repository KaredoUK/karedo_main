package karedo.entity

import java.util.UUID

import karedo.entity.dao._
import karedo.util.{KO, OK, Result}
import org.joda.time.DateTime
import salat.annotations._
import karedo.util.Util.now

import scala.util.{Failure, Success, Try}


case class UserAccount
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , info: Option[String] = None
  , password: Option[String] = None
  , userType: String = "CUSTOMER"
  , mobile: List[Mobile] = List()
  , email: List[Email] = List()
  , temp: Boolean = true
  , ts_created: DateTime = now
  , ts_updated: DateTime = now

) extends Keyable[String] with UserAccountDefs

sealed trait UserAccountDefs {
  self: UserAccount =>

  def findActiveMobile: Result[String, Mobile] = {
    Try {
      mobile.filter(_.valid).reduce((c,z) => if (c.ts_validated.get.compareTo(z.ts_validated.get) > 0) c else z )
//        .foldLeft[Mobile](null)((c, z) => {
//        if (c == null) z // First time.
//        else {
//          if (c.ts_validated.get.compareTo(z.ts_validated.get) > 0) c else z
//        }
//      })
    } match {
      case Success(x) => OK(x)
      case Failure(error) => KO(error.toString)
    }
  }

  def findActiveEmail: Result[String, Email] = {
    Try {
      email.filter(_.valid).reduce((c,z) => if (c.ts_validated.get.compareTo(z.ts_validated.get) > 0) c else z )
//        List().foldLeft[Email](null)((c, z) => {
//        if (c == null) z // First time.
//        else {
//          if (c.ts_validated.get.compareTo(z.ts_validated.get) > 0) c else z
//        }
//      })
    } match {
      case Success(x) => OK(x)
      case Failure(error) => KO(error.toString)
    }
  }
}

case class Mobile
(
  msisdn: String
  , sms_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = now
  , ts_validated: Option[DateTime] = None
  // Chandan - took this out, beacuse not sure how to use it yet.
  //  , active: Boolean = false
)

case class Email
(
  address: String
  , email_code: Option[String] = None
  , valid: Boolean = false
  , ts_created: DateTime = now
  , ts_validated: Option[DateTime] = None
)

trait DbUserAccount extends DbMongoDAO1[String,UserAccount] {
  def findActiveMobile(id: String): Result[String, Mobile] = {
    find(id) match {
      case OK(userAccount) => userAccount.findActiveMobile
      case KO(k) => KO(k)
    }
  }

  def findActiveEmail(id: String): Result[String, Email] = {
    find(id) match {
      case OK(userAccount) => userAccount.findActiveEmail
      case KO(k) => KO(k)
    }
  }
}
