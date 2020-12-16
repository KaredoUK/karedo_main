package restapi

import java.util.UUID
import java.util.UUID._



import restapi.security.AuthenticationSupport
import com.parallelai.wallet.datamanager.data._
import core.EditAccountActor
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
  with RetryExamples // allows repetition of tests if they are temporarily failing
{
  sequential
  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  override def responseTimeout = 30.seconds

  lazy val exampleP77 = {
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

  lazy val exampleP101 = {
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

  lazy val exampleP53 =  {
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

  lazy val exampleP102 = {
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

  lazy val exampleP51 = {
      "Retrieve user profile for the calling authenticated user" in new WithMockedPersistenceRestService {

        val sessionId = randomUUID()

        val userAccount = UserAccount(randomUUID(), Some("Email"), Some("msisdn"))

        // Authentication is fine
        mockedUserSessionDAO.getValidSessionAndRenew(sessionId) returns
          Some(UserSession(sessionId, userAccount.id, randomUUID()))

        // there is a client application for that
        mockedClientApplicationDAO.findByUserId(userAccount.id) returns
          Seq(ClientApplication(randomUUID(), userAccount.id, "activationCode", true))

        mockedUserAccountDAO.getById(userAccount.id) returns Some(userAccount)

        val pipeline = addHeader(AuthenticationSupport.HEADER_NAME_SESSION_ID, sessionId.toString) ~>
          sendReceive ~> unmarshal[UserProfile]

        val returnedProfile = wait { pipeline { Get(s"$serviceUrl/account/${userAccount.id}")  } }

        returnedProfile shouldEqual EditAccountActor.userAccountToUserProfile(userAccount)
      }

      "Refuse unauthenticated user" in new WithMockedPersistenceRestService {
        val userAccount = UserAccount(randomUUID(), Some("Email"), Some("msisdn"))

        mockedUserAccountDAO.getById(userAccount.id) returns Some(userAccount)

        val pipeline = sendReceive

        val returnedProfile = wait { pipeline { Get(s"$serviceUrl/account/${userAccount.id}")  } }

        returnedProfile.status shouldEqual Unauthorized
      }

      "Refuse calls from a different authenticated user" in new WithMockedPersistenceRestService {
        val sessionId = randomUUID()

        val userAccount = UserAccount(randomUUID(), Some("Email"), Some("msisdn"))

        val otherUserId = randomUUID()

        // Authentication is with another user
        mockedUserSessionDAO.getValidSessionAndRenew(sessionId) returns
          Some(UserSession(sessionId, otherUserId, randomUUID()))

        // there is a client application for the other user
        mockedClientApplicationDAO.findByUserId(otherUserId) returns
          Seq(ClientApplication(randomUUID(), userAccount.id, "activationCode", true))

        mockedUserAccountDAO.getById(otherUserId) returns Some(userAccount)

        val pipeline = addHeader(AuthenticationSupport.HEADER_NAME_SESSION_ID, sessionId.toString) ~>
          sendReceive

        val returnedProfile = wait { pipeline { Get(s"$serviceUrl/account/${userAccount.id}")  } }

        returnedProfile.status shouldEqual Forbidden
      }
    }

  lazy val exampleP54 =  {

      "Return user points for the calling authenticated user" in new WithMockedPersistenceRestService {
        val sessionId = randomUUID()

        val pipeline = addHeader(AuthenticationSupport.HEADER_NAME_SESSION_ID, sessionId.toString) ~>
          sendReceive ~> unmarshal[UserPoints]



        val userAccount = UserAccount(randomUUID(), Some("Email"), Some("msisdn"), totalPoints = 5000)

        // Authentication is fine
        mockedUserSessionDAO.getValidSessionAndRenew(sessionId) returns
          Some(UserSession(sessionId, userAccount.id, randomUUID()))

        // there is a client application for that
        mockedClientApplicationDAO.findByUserId(userAccount.id) returns
          Seq(ClientApplication(randomUUID(), userAccount.id, "activationCode", true))

        mockedUserAccountDAO.getById(userAccount.id) returns Some(userAccount)

        val returnedPoints:UserPoints = wait {
          pipeline { Get(s"$serviceUrl/account/${userAccount.id}/points")  } }

        returnedPoints.totalPoints shouldEqual(5000)

      }

      "Refuse unauthenticated user" in new WithMockedPersistenceRestService {
        val userAccount = UserAccount(randomUUID(), Some("Email"), Some("msisdn"))

        mockedUserAccountDAO.getById(userAccount.id) returns Some(userAccount)

        val pipeline = sendReceive

        val returnedPointsResponse = wait { pipeline { Get(s"$serviceUrl/account/${userAccount.id}/points")  } }

        returnedPointsResponse.status shouldEqual Unauthorized
      }

      "Refuse calls from a different authenticated user" in new WithMockedPersistenceRestService {
        val sessionId = randomUUID()

        val userAccount = UserAccount(randomUUID(), Some("Email"), Some("msisdn"))

        val otherUserId = randomUUID()

        // Authentication is with another user
        mockedUserSessionDAO.getValidSessionAndRenew(sessionId) returns
          Some(UserSession(sessionId, otherUserId, randomUUID()))

        // there is a client application for the other user
        mockedClientApplicationDAO.findByUserId(otherUserId) returns
          Seq(ClientApplication(randomUUID(), userAccount.id, "activationCode", true))

        mockedUserAccountDAO.getById(otherUserId) returns Some(userAccount)

        val pipeline = addHeader(AuthenticationSupport.HEADER_NAME_SESSION_ID, sessionId.toString) ~>
          sendReceive

        val returnedPointsResponse = wait { pipeline { Get(s"$serviceUrl/account/${userAccount.id}/points")  } }

        returnedPointsResponse.status shouldEqual Forbidden
      }
    }

  "PARALLELAI-77:Account Service API" should  exampleP77
  "PARALLELAI-101: Account Service API" should exampleP101
  "PARALLELAI-53: Account Service API" should exampleP53
  "PARALLELAI-102: login" should exampleP102
  "PARALLELAI-51 get user profile" should exampleP51
  "PARALLELAI-54API: Get User Points" should exampleP54


}
