package api

import java.util.UUID
import java.util.UUID._

import com.parallelai.wallet.datamanager.data._
import org.apache.commons.lang.StringUtils
import org.specs2.matcher._
import org.specs2.mutable.Specification
import parallelai.wallet.entity.{UserSession, UserAccount, ClientApplication}
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

  "PARALLELAI-77:Account Service API" should {
    " Register a new user using MSISDN inserting a new account with validation code in the DB" in
      new WithMockedPersistenceRestService {
        val pipeline = sendReceive ~> unmarshal[RegistrationResponse]

        mockedClientApplicationDAO.getById(any[UUID]) returns None
        mockedUserAccountDAO.findByAnyOf(any[Option[UUID]], any[Option[String]], any[Option[String]]) returns None

        val applicationId = randomUUID()
        val msisdn = "00123123123"
        val registrationResponse = wait {
          pipeline {
            Post(s"$serviceUrl/account", RegistrationRequest(applicationId, Some(msisdn), None))
          }
        }

        registrationResponse shouldEqual RegistrationResponse(applicationId, "msisdn", msisdn)

        there was one(mockedUserAccountDAO).insertNew(
          userAccount = argThat(haveMsisdn(msisdn)),
          firstApplication = argThat(
            beInactive and
              haveActivationCode and
              beAnAppWithId(applicationId))
        )
      }

    "Refuse registration with no MSISDN or email" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive
      val registrationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account", RegistrationRequest(randomUUID(), None, None))
        }
      }

      registrationResponse should haveStatusCode(BadRequest)
    }
  }

  "PARALLELAI-101: Account Service API" should {
    "Add an application to an existing account" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[AddApplicationResponse]

      val msisdn = "00123123123"
      val userAccount = UserAccount(randomUUID(), Some(msisdn), Some("email"))

      mockedClientApplicationDAO.getById(any[UUID]) returns None
      mockedUserAccountDAO.getById(any[UUID]) returns Some(userAccount)
      mockedUserAccountDAO.findByAnyOf(
        any[Option[UUID]], any[Option[String]], any[Option[String]]
      ) returns Some(userAccount)

      val applicationId = randomUUID()

      val registrationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application", AddApplicationRequest(applicationId, Some(msisdn), None))
        }
      }

      registrationResponse shouldEqual AddApplicationResponse(applicationId, "msisdn", msisdn)

      there was no(mockedUserAccountDAO).insertNew(any[UserAccount], any[ClientApplication])
      there was one(mockedClientApplicationDAO).insertNew(ClientApplication(applicationId, userAccount.id, anyString))
    }
  }

  "PARALLELAI-53: Account Service API" should {
    "Activate user if activationCode is correct" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[RegistrationValidationResponse]

      val user = UserAccount(randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode")


      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "activationCode", Some("pwd")))
        }
      }

      validationResponse shouldEqual RegistrationValidationResponse(clientApplication.id, user.id)

      there was one(mockedUserAccountDAO).setActive( argEq(user.id) )
    }

    "Refuse wrong activationCode" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val user = UserAccount(randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode")

      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "wrongActivationCode", Some("pwd")))
        }
      }

      validationResponse should haveStatusCode(Unauthorized)

      there was no(mockedUserAccountDAO).setActive( any[UUID] )
    }

    "Save password for a newly activated user " in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[RegistrationValidationResponse]

      val user = UserAccount(id = randomUUID(), msisdn = Some("123123"), email = None, password = None)
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode")


      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "activationCode", Some("pwd")))
        }
      }

      there was one(mockedUserAccountDAO).setPassword( argEq(user.id), argEq("pwd") )
    }

    "Ignore password for a user with an already set one" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[RegistrationValidationResponse]

      val user = UserAccount(id = randomUUID(), msisdn = Some("123123"), email = None, password = Some("pwd"))
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode")


      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "activationCode", Some("pwd")))
        }
      }

      there was no(mockedUserAccountDAO).setPassword( argEq(user.id), anyString )
    }

    "Fail if no password is supplied for a newly activated user " in new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val user = UserAccount(id = randomUUID(), msisdn = Some("123123"), email = None, password = None)
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode")

      mockedClientApplicationDAO.getById(any[UUID]) returns Some(clientApplication)
      mockedUserAccountDAO.getByApplicationId(any[UUID], any[Boolean]) returns Some(user)

      val validationResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/application/validation", RegistrationValidation(clientApplication.id, "activationCode", None))
        }
      }

      validationResponse.status shouldEqual StatusCodes.BadRequest
    }

  }

  "PARALLELAI-102: login" should {
    "Accept user with correct password and active application returning new sessionId" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[APISessionResponse]

      val user = UserAccount(randomUUID(), Some("123123"), None, password = Some("password"))
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode", active = true)
      val userSession = UserSession(randomUUID(), user.id, clientApplication.id)

      mockedClientApplicationDAO.getById(clientApplication.id) returns Some(clientApplication)
      mockedUserAccountDAO.getById(user.id) returns Some(user)
      mockedUserSessionDAO.createNewSession(user.id, clientApplication.id) returns userSession

      val loginResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/${user.id}/application/${clientApplication.id}/login", APILoginRequest(password = "password"))
        }
      }
      loginResponse shouldEqual APISessionResponse(userSession.sessionId.toString)
    }

    "Refuse user with correct password but inactive application" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val user = UserAccount(randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode", active = false)
      val userSession = UserSession(randomUUID(), user.id, clientApplication.id)

      mockedClientApplicationDAO.getById(clientApplication.id) returns Some(clientApplication)
      mockedUserAccountDAO.getById(user.id) returns Some(user)

      val loginResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/${user.id}/application/${clientApplication.id}/login", APILoginRequest(password = "password"))
        }
      }

      loginResponse.status shouldEqual Unauthorized

      there was no(mockedUserSessionDAO).createNewSession(any[UUID], any[UUID])
    }

    "Refuse user with incorrect password" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val user = UserAccount(randomUUID(), Some("123123"), None, password = Some("otherPassword"))
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode", active = true)
      val userSession = UserSession(randomUUID(), user.id, clientApplication.id)

      mockedClientApplicationDAO.getById(clientApplication.id) returns Some(clientApplication)
      mockedUserAccountDAO.getById(user.id) returns Some(user)

      val loginResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/${user.id}/application/${clientApplication.id}/login", APILoginRequest(password = "password"))
        }
      }

      loginResponse.status shouldEqual Unauthorized

      there was no(mockedUserSessionDAO).createNewSession(any[UUID], any[UUID])
    }

    "Refuse user with no password" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive

      val user = UserAccount(randomUUID(), Some("123123"), None)
      val clientApplication = ClientApplication(randomUUID, user.id, "activationCode", active = true)
      val userSession = UserSession(randomUUID(), user.id, clientApplication.id)

      mockedClientApplicationDAO.getById(clientApplication.id) returns Some(clientApplication)
      mockedUserAccountDAO.getById(user.id) returns Some(user)

      val loginResponse = wait {
        pipeline {
          Post(s"$serviceUrl/account/${user.id}/application/${clientApplication.id}/login", APILoginRequest(password = "password"))
        }
      }

      loginResponse.status shouldEqual Unauthorized

      there was no(mockedUserSessionDAO).createNewSession(any[UUID], any[UUID])
    }
  }




}
