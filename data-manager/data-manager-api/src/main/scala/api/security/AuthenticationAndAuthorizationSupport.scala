package api.security

import java.util.UUID

import parallelai.wallet.entity.UserAuthContext
import parallelai.wallet.persistence.UserAuthDAO
import spray.http.HttpHeaders.RawHeader
import spray.http.{HttpHeader, HttpRequest}
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.{AuthMagnet, SecurityDirectives}

import scala.concurrent.Future._
import scala.concurrent.{ExecutionContext, Future}

object AuthenticationSupport {
  val HEADER_NAME_SESSION_ID = "X-SESSION-ID"
  val HEADER_SESSION_ID: HttpHeader = RawHeader(HEADER_NAME_SESSION_ID, "")

  def extractSessionIDHeader(request: HttpRequest): Option[String] =
    request.headers.find { header => header.name == HEADER_NAME_SESSION_ID } map { _.value } filterNot { _.isEmpty }
  
}

trait AuthenticationSupport {
  self: SecurityDirectives =>
  
  import AuthenticationSupport._

  protected def authDAO: UserAuthDAO
  protected def executionContext: ExecutionContext

  implicit private val _execCtx = executionContext

  import spray.routing.authentication._
  def userAuthContextFromSessionId(authDAO: UserAuthDAO)(requestCtx: RequestContext): Future[Authentication[UserAuthContext]] = {
    val sessionIdOp = extractSessionIDHeader(requestCtx.request)

    sessionIdOp map { sessionId =>
      authDAO.getUserContextForSession(sessionId) map { userContextForSessionOp =>
        userContextForSessionOp map {
          Right(_)
        } getOrElse {
          Left(AuthenticationFailedRejection(CredentialsRejected, List(HEADER_SESSION_ID)))
        }
      }
    } getOrElse {
      successful{
        Left( AuthenticationFailedRejection(CredentialsMissing, List(HEADER_SESSION_ID)) )
      }
    }
  }

  def userContextAuthenticator: ContextAuthenticator[UserAuthContext] = userAuthContextFromSessionId(authDAO)

  def authenticateWithKaredoSession: Directive1[UserAuthContext] =
    authenticate( AuthMagnet.fromContextAuthenticator(userContextAuthenticator) )
}

trait AuthorizationSupport extends AuthenticationSupport {
  self: SecurityDirectives =>

  type KaredoAuthCheck = UserAuthContext => Boolean

  def isLoggedInUser(userAuthContext: UserAuthContext): Boolean = true
  def canAccessUser(userId: UUID)(userAuthContext: UserAuthContext): Boolean = userAuthContext.userId == userId
  def hasActiveAppWithID(appID: UUID)(userAuthContext: UserAuthContext): Boolean = userAuthContext.activeApps.contains(appID)
  
  def userAuthorizedFor( check: => KaredoAuthCheck ): Directive1[UserAuthContext] =
    authenticateWithKaredoSession.flatMap { userAuthContext: UserAuthContext =>
      authorize( check(userAuthContext) ).hflatMap {
        _ => provide(userAuthContext)
      }
    }

}
