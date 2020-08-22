package api

import java.util.UUID

import com.parallelai.wallet.datamanager.data.{RegistrationRequest, RegistrationResponse}
import org.specs2.matcher.{MatchSuccess, MatchResult, Expectable, Matcher}
import org.specs2.mutable.Specification
import parallelai.wallet.entity.{UserAccount, ClientApplication}
import spray.client.pipelining._
import util.ApiHttpClientSpec
import scala.concurrent.Future._
import scala.concurrent.duration._

class AccountServiceSpec extends ApiHttpClientSpec {
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._


  override def responseTimeout = 30.seconds

//  val inactiveApp = new Matcher[ClientApplication] {
//    override def apply[S <: ClientApplication](t: Expectable[S]): MatchResult[S] = {
//      t.value.active shouldEqual false
//    }
//  }

  "Account Service API" should {
    "Register a new user using MSISDN inserting a new account with validation code in the DB" in {
      val pipeline = sendReceive ~> unmarshal[RegistrationResponse]

      mockedClientApplicationDAO.getById(any[UUID]) returns None
      mockedUserAccountDAO.findByAnyOf(any[Option[UUID]], any[Option[String]], any[Option[String]]) returns None

      val applicationId = UUID.randomUUID()
      val msisdn = "00123123123"
      val registrationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account", RegistrationRequest(applicationId, Some(msisdn), None))
        }
      }

      registrationResponse shouldEqual RegistrationResponse(applicationId, "msisdn", msisdn)

//      there was one(mockedClientApplicationDAO).insertNew(argThat( inactiveApp ) )
      there was one(mockedUserAccountDAO).insertNew(any[UserAccount], any[ClientApplication])
    }
  }
}
