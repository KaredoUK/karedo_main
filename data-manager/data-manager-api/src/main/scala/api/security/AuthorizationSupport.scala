package api.security

import java.util.UUID

import parallelai.wallet.entity.UserAuthContext
import spray.routing.Directive1
import spray.routing.directives.BasicDirectives._
import spray.routing.directives.RouteDirectives._
import spray.routing.directives.SecurityDirectives

trait AuthorizationSupport extends SecurityDirectives {
  self: AuthenticationSupport =>

  def canAccessUser(userId: UUID)(userAuthContext: UserAuthContext): Boolean = userAuthContext.userId == userId

  type KaredoAuthCheck = UserAuthContext => Boolean

  def userAuthorizedFor( check: => KaredoAuthCheck ): Directive1[UserAuthContext] =
    authenticateWithKaredoSession.flatMap { userAuthContext: UserAuthContext =>
      authorize( check(userAuthContext) ).hflatMap {
        _ => provide(userAuthContext)
      }
  }

}
