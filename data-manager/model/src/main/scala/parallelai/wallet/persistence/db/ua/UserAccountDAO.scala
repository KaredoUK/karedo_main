package parallelai.wallet.persistence.db.ua

import java.util.UUID

import com.mongodb.WriteResult

import scala.util.Try


trait UserAccountDAO {
  def insertNew(userAccount: UserAccount) : Try[Option[UUID]]
  def getById(userId: UUID) : Option[UserAccount]
  def update(userAccount: UserAccount): Try[WriteResult]
  def delete(userId: UUID) : Try[WriteResult]
//  def delete(userId: UUID) : Unit
/*

  def getByMsisdn(msisdn: String, mustBeActive: Boolean = false) : Option[UserAccount]
  def getByEmail(email: String, mustBeActive: Boolean = false) : Option[UserAccount]
  def findByAnyOf(msisdn: Option[String], email: Option[String]) : Option[UserAccount]


  def setActive(userId : UUID): Unit
  def setEmail(userId: UUID, email: String) : Unit
  def setMsisdn(userId: UUID, msisdn: String) : Unit
  def setPassword(userId: UUID, password: String) : Unit
  def checkPassword(userId: UUID, password: String): Boolean

*/

}


