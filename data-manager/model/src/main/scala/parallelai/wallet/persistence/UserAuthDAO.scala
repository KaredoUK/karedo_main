package parallelai.wallet.persistence

import parallelai.wallet.entity.UserAuthContext

trait UserAuthDAO {
  def getUserContextForSession(sessionId: String): Option[UserAuthContext]
}
