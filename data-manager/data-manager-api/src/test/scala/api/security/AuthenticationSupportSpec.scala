package api.security

import java.util.UUID
import java.util.UUID._

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import parallelai.wallet.entity.UserAuthContext
import parallelai.wallet.persistence.UserAuthDAO
import spray.http.StatusCodes
import spray.http.StatusCodes._
import spray.routing.AuthenticationFailedRejection.{CredentialsRejected, CredentialsMissing}
import spray.testkit.Specs2RouteTest
import spray.routing.{AuthenticationFailedRejection, Directives, HttpService}

import scala.concurrent.Future._
import scala.concurrent.{Future, ExecutionContext}
import AuthenticationSupport._
import org.specs2.specification.Scope


class AuthenticationSupportSpec extends Specification with Specs2RouteTest with HttpService with Mockito {
  def actorRefFactory = system

  trait WithAuthenticatedRoute extends Scope with Directives with AuthenticationSupport {
    val mockAuthDAO = mock[UserAuthDAO]

    override def authDAO: UserAuthDAO = mockAuthDAO

    override def executionContext: ExecutionContext = system.dispatcher

    val testRoute =
      path("authenticatedRoute") {
        authenticateWithKaredoSession { userAuthContext =>
          get {
            complete {
              userAuthContext.toString
            }
          }
        }
      }
  }


  "An AuthenticatedRoute" should {
    "Extract user authentication context" in new WithAuthenticatedRoute {
      val userAuthContext = UserAuthContext(randomUUID(), Seq(randomUUID(), randomUUID()))
      val sessionId = "sessionId"
      mockAuthDAO.getUserContextForSession(sessionId) returns successful { Some(userAuthContext) }

      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> testRoute ~> check {
        responseAs[String] mustEqual userAuthContext.toString
      }
    }

    "Refuse request with missing session id" in new WithAuthenticatedRoute {
      Get("/authenticatedRoute") ~> testRoute ~> check {
        rejection mustEqual AuthenticationFailedRejection(CredentialsMissing, List(HEADER_SESSION_ID))
      }
    }

    "Refuse request with empty session id" in new WithAuthenticatedRoute  {
      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, "") ~> testRoute ~> check {
        rejection mustEqual AuthenticationFailedRejection(CredentialsMissing, List(HEADER_SESSION_ID))
      }
    }

    "Refuse request with non matching session id" in new WithAuthenticatedRoute  {
      val sessionId = "sessionId"
      mockAuthDAO.getUserContextForSession(sessionId) returns successful { None }

      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> testRoute ~> check {
        rejection mustEqual AuthenticationFailedRejection(CredentialsRejected, List(HEADER_SESSION_ID))
      }
    }

    "Fail with internal error when having problems retrieving sessions" in new WithAuthenticatedRoute  {
      val sessionId = "sessionId"
      mockAuthDAO.getUserContextForSession(sessionId) returns failed { new Exception("Internal error") }

      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> testRoute ~> check {
        response.status mustEqual InternalServerError
      }
    }
  }

}
