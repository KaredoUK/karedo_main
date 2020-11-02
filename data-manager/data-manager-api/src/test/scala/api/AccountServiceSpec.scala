package api

import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import org.apache.commons.lang.StringUtils
import org.specs2.matcher._
import org.specs2.mutable.Specification
import parallelai.wallet.entity.{UserAccount, ClientApplication}
import spray.client.pipelining._
import spray.http.StatusCodes._
import spray.http.{StatusCodes, StatusCode, HttpResponse}
import util.{RestApiSpecMatchers, ApiHttpClientSpec}
import scala.concurrent.Future._
import scala.concurrent.duration._
import org.specs2.mutable.SpecificationLike
import org.mockito.Matchers.{eq => argEq}


class AccountServiceSpec
  extends ApiHttpClientSpec // Mocked instance
  with RestApiSpecMatchers  // specialized Matchers
{
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  override def responseTimeout = 30.seconds

  "Account Service API" should {
    "PARALLELAI-77: Register a new user using MSISDN inserting a new account with validation code in the DB" in
      new WithMockedPersistenceRestService
      {
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

        there was one(mockedUserAccountDAO).insertNew(
          userAccount = argThat( haveMsisdn(msisdn) ),
          firstApplication = argThat(
            beInactive and
              haveActivationCode and
              beAnAppWithId(applicationId) )
        )
    }

    "PARALLELAI-101: Add an application to an existing account" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[AddApplicationResponse]

      val msisdn = "00123123123"
      val userAccount = UserAccount(UUID.randomUUID(), Some(msisdn), Some("email"))

      mockedClientApplicationDAO.getById(any[UUID]) returns None
      mockedUserAccountDAO.getById(any[UUID]) returns Some(userAccount)
      mockedUserAccountDAO.findByAnyOf(
        any[Option[UUID]], any[Option[String]], any[Option[String]]
      ) returns Some(userAccount)

      val applicationId = UUID.randomUUID()

      val registrationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application", AddApplicationRequest(applicationId, Some(msisdn), None))
        }
      }

      registrationResponse shouldEqual AddApplicationResponse(applicationId, "msisdn", msisdn)

      there was no(mockedUserAccountDAO).insertNew( any[UserAccount], any[ClientApplication] )
      there was one(mockedClientApplicationDAO).insertNew(ClientApplication(applicationId, userAccount.id, anyString))
    }

    "PARALLELAI-77b: Refuse registration with no MSISDN or email" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive
      val registrationResponse = wait {
       pipeline {
          Post(s"$serviceUrl/account", RegistrationRequest(UUID.randomUUID(), None, None))
        }
      }

      registrationResponse should haveStatusCode(BadRequest)
    }

    "PARALLELAI-53: Activate user if activationCode is correct" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[RegistrationValidationResponse]

      val user = UserAccount(UUID.randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(UUID.randomUUID, user.id, "activationCode")


      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "activationCode"))
        }
      }

      validationResponse shouldEqual RegistrationValidationResponse(clientApplication.id, user.id)

      there was one(mockedUserAccountDAO).setActive( argEq(user.id) )
    }

    "PARALLELAI-53b: Refuse wrong activationCode" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val user = UserAccount(UUID.randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(UUID.randomUUID, user.id, "activationCode")

      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "wrongActivationCode"))
        }
      }

      validationResponse should haveStatusCode(Unauthorized)

      there was no(mockedUserAccountDAO).setActive( any[UUID] )
    }

    "PARALLELAI-102: login" in new WithMockedPersistenceRestService {

      val pipeline = sendReceive ~> unmarshal[APISessionResponse]

      val user = UserAccount(UUID.randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(UUID.randomUUID, user.id, "activationCode")


      val loginResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/${user.id}/application/${clientApplication.id}/login", APILoginRequest(password="password"))
        }
      }
      UUID.fromString(loginResponse.sessionId).toString shouldEqual loginResponse.sessionId

    }
  }




}
