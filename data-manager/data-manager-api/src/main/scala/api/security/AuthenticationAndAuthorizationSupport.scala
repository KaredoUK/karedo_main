package api.security

import java.util.UUID

import parallelai.wallet.entity.UserAuthContext
import parallelai.wallet.persistence.UserAuthDAO
import spray.http.HttpHeaders.RawHeader
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.{AuthMagnet, SecurityDirectives}

import scala.concurrent.{ExecutionContext, Future}

trait AuthenticationSupport {
  self: SecurityDirectives =>

  def authDAO: UserAuthDAO
  def executionContext: ExecutionContext

  implicit private val _execCtx = executionContext

  import spray.routing.authentication._
  def userAuthContextFromSessionId(authDAO: UserAuthDAO)(requestCtx: RequestContext): Future[Authentication[UserAuthContext]] = Future {
    val sessionIdOp = requestCtx.request.headers.find { header => header.name == "X-SESSION-ID" } map { _.value }

    sessionIdOp map { sessionId =>
      authDAO.getUserContextForSession(sessionId) map {
        Right(_)
      } getOrElse {
        Left( AuthenticationFailedRejection( CredentialsRejected, List(RawHeader("X-SESSION-ID", "")) ) )
      }
    } getOrElse {
      Left( AuthenticationFailedRejection(CredentialsMissing, List(RawHeader("X-SESSION-ID", ""))) )
    }
  }

  def userContextAuthenticator: ContextAuthenticator[UserAuthContext] = userAuthContextFromSessionId(authDAO)

  def authenticateWithKaredoSession: Directive1[UserAuthContext] = authenticate( AuthMagnet.fromContextAuthenticator(userContextAuthenticator) )
}

trait AuthorizationSupport extends AuthenticationSupport {
  self: SecurityDirectives =>

  def canAccessUser(userId: UUID)(userAuthContext: UserAuthContext): Boolean = userAuthContext.userId == userId

  type KaredoAuthCheck = UserAuthContext => Boolean

  def userAuthorizedFor( check: => KaredoAuthCheck ): Directive1[UserAuthContext] =
    authenticateWithKaredoSession.flatMap { userAuthContext: UserAuthContext =>
      authorize( check(userAuthContext) ).hflatMap {
        _ => provide(userAuthContext)
      }
    }

}
