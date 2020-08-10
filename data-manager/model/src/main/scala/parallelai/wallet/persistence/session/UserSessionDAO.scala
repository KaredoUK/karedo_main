package parallelai.wallet.persistence.session

import java.util.UUID

import scala.concurrent.Future

trait UserSessionDAO {
  def get(userId: UUID): Future[Option[UserSessionData]]
  def store(userId: UUID, userSession: UserSessionData): Future[Unit]
}
