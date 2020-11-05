package api.security

import parallelai.wallet.entity.UserAuthContext
import parallelai.wallet.persistence.UserAuthDAO
import spray.http.HttpHeaders.RawHeader
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.directives.{AuthMagnet, SecurityDirectives}

import scala.concurrent.Future

trait AuthenticationSupport extends SecurityDirectives {
  def authDAO: UserAuthDAO

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
