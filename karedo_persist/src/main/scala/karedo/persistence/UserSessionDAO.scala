package karedo.persistence

import java.util.UUID

import karedo.entity.UserSession
import karedo.persistence.mongodb.MongoSession

trait UserSessionDAO {
  def isValidSession(sessionId: UUID): Boolean
  def getValidSessionAndRenew(sessionId: UUID): Option[UserSession]
  def createNewSession(userId: UUID, applicationId: UUID): UserSession
  def deleteSession(sessionId: UUID)
}
