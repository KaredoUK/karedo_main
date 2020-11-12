package parallelai.wallet.persistence

import java.util.UUID

import parallelai.wallet.entity.UserSession
import parallelai.wallet.persistence.mongodb.MongoSession

trait UserSessionDAO {
  def isValidSession(sessionId: UUID): Boolean
  def getValidSessionAndRenew(sessionId: UUID): Option[UserSession]
  def createNewSession(userId: UUID, applicationId: UUID): UserSession
  def deleteSession(sessionId: UUID)
}
