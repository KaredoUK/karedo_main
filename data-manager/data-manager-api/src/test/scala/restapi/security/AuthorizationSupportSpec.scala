package restapi.security

import java.util.UUID._

import restapi.RetryExamples
import restapi.security.AuthenticationSupport._
import core.security.UserAuthService
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import parallelai.wallet.entity.UserAuthContext
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing.{AuthorizationFailedRejection, AuthenticationFailedRejection, Directives, HttpService}
import spray.testkit.Specs2RouteTest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future._


class AuthorizationSupportSpec
  extends Specification
  with Specs2RouteTest
  with HttpService
  with RetryExamples
  with Mockito {
  def actorRefFactory = system

  trait WithAuthenticatedRoute extends Scope with Directives with AuthorizationSupport {
    val mockAuthService = mock[UserAuthService]

    override def userAuthService: UserAuthService = mockAuthService

    def testRoute(check: KaredoAuthCheck) =
      path("authenticatedRoute") {
        userAuthorizedFor( check )(system.dispatcher) { userAuthContext =>
          get {
            complete {
              userAuthContext.toString
            }
          }
        }
      }
  }

  "An AuthorizedRoute" should {
    "Extract user authentication context" in new WithAuthenticatedRoute {
      val userAuthContext = UserAuthContext(randomUUID(), Seq(randomUUID(), randomUUID()))
      val sessionId = "sessionId"
      mockAuthService.getUserContextForSession(sessionId) returns successful { Some(userAuthContext) }

      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> testRoute(isLoggedInUser) ~> check {
        responseAs[String] mustEqual userAuthContext.toString
      }
    }

    "Refuse request with missing session id" in new WithAuthenticatedRoute {
      Get("/authenticatedRoute") ~> testRoute(isLoggedInUser) ~> check {
        rejection mustEqual AuthenticationFailedRejection(CredentialsMissing, List(HEADER_SESSION_ID))
      }
    }

    "Refuse request with empty session id" in new WithAuthenticatedRoute  {
      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, "") ~> testRoute(isLoggedInUser) ~> check {
        rejection mustEqual AuthenticationFailedRejection(CredentialsMissing, List(HEADER_SESSION_ID))
      }
    }

    "Refuse request with non matching session id" in new WithAuthenticatedRoute  {
      val sessionId = "sessionId"
      mockAuthService.getUserContextForSession(sessionId) returns successful { None }

      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> testRoute(isLoggedInUser) ~> check {
        rejection mustEqual AuthenticationFailedRejection(CredentialsRejected, List(HEADER_SESSION_ID))
      }
    }

    "Refuse logged in users not passing validation check" in new WithAuthenticatedRoute  {
      val userAuthContext = UserAuthContext(randomUUID(), Seq(randomUUID(), randomUUID()))
      val sessionId = "sessionId"
      mockAuthService.getUserContextForSession(sessionId) returns successful { Some(userAuthContext) }

      Get("/authenticatedRoute") ~> addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> testRoute(hasActiveAppWithID(randomUUID())) ~> check {
        rejection mustEqual AuthorizationFailedRejection
      }
    }
  }

}
