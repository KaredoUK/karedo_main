package core.security

import java.util.UUID

import parallelai.wallet.entity.UserAuthContext
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO, UserSessionDAO}

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

trait UserAuthService {
  def getUserContextForSession(sessionId: String): Future[Option[UserAuthContext]]
}

class UserAuthServiceImpl(sessionDAO: UserSessionDAO, clientApplicationDAO: ClientApplicationDAO) extends UserAuthService {
  override def getUserContextForSession(sessionId: String): Future[Option[UserAuthContext]] = Future {
    val sessionUUID = UUID.fromString(sessionId)

    sessionDAO.getValidSessionAndRenew(sessionUUID) map { session =>
      val activeAppsIDs = clientApplicationDAO.findByUserId(session.userId) filter { _.active } map { _.id }

      UserAuthContext(session.userId, activeAppsIDs)
    }
  }
}