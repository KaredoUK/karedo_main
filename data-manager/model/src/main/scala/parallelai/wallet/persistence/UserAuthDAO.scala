package parallelai.wallet.persistence

import parallelai.wallet.entity.UserAuthContext

import scala.concurrent.Future

//This is an Async DAO since we are going to implement it with a backing typed actor
trait UserAuthDAO {
  def getUserContextForSession(sessionId: String): Future[Option[UserAuthContext]]
}
