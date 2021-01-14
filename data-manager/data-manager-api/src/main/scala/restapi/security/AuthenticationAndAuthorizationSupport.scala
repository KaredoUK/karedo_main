package restapi.security

import java.util.UUID
import core.security.UserAuthService
import parallelai.wallet.entity.UserAuthContext
import spray.http.HttpHeaders.RawHeader
import spray.http.{HttpHeader, HttpRequest}
import spray.routing.AuthenticationFailedRejection.{CredentialsMissing, CredentialsRejected}
import spray.routing._
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.{AuthMagnet, SecurityDirectives}
import scala.concurrent.Future._
import scala.concurrent.{ExecutionContext, Future}
import core.WrapLog
import spray.util.SprayActorLogging

object AuthenticationSupport {
  val HEADER_NAME_SESSION_ID = "X-Session-Id"
  val HEADER_SESSION_ID: HttpHeader = RawHeader(HEADER_NAME_SESSION_ID, "")

  def extractSessionIDHeader(request: HttpRequest): Option[String] =
    request.headers.find { 
      header => header.name == HEADER_NAME_SESSION_ID 
    } map { _.value } filterNot { _.isEmpty }
  
}

trait AuthenticationSupport  {
  self: SecurityDirectives =>
  
  import AuthenticationSupport._

  protected def userAuthService: UserAuthService

  import spray.routing.authentication._
  def userAuthContextFromSessionId(authService: UserAuthService)(implicit executionContext: ExecutionContext): ContextAuthenticator[UserAuthContext] =  (requestCtx: RequestContext) =>  {
    val sessionIdOp = extractSessionIDHeader(requestCtx.request)

    sessionIdOp map { sessionId =>
      authService.getUserContextForSession(sessionId) map { userContextForSessionOp =>
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

  def userContextAuthenticator(implicit executionContext: ExecutionContext): ContextAuthenticator[UserAuthContext] = userAuthContextFromSessionId(userAuthService)

  def authenticateWithKaredoSession(implicit executionContext: ExecutionContext): Directive1[UserAuthContext] =
    authenticate( AuthMagnet.fromContextAuthenticator(userContextAuthenticator) )
}

trait AuthorizationSupport extends AuthenticationSupport {
  self: SecurityDirectives =>

  type KaredoAuthCheck = UserAuthContext => Boolean

  def isLoggedInUser(userAuthContext: UserAuthContext): Boolean = true
  def canAccessUser(userId: UUID)(userAuthContext: UserAuthContext): Boolean = userAuthContext.userId == userId
  def hasActiveAppWithID(appID: UUID)(userAuthContext: UserAuthContext): Boolean = userAuthContext.activeApps.contains(appID)
  
  def userAuthorizedFor( check: => KaredoAuthCheck )(implicit executionContext: ExecutionContext): Directive1[UserAuthContext] =
    authenticateWithKaredoSession.flatMap { userAuthContext: UserAuthContext =>
      authorize( check(userAuthContext) ).hflatMap {
        _ => provide(userAuthContext)
      }
    }

}
