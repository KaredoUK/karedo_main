package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.annotations._
import com.novus.salat.dao.SalatDAO
import org.joda.time.DateTime
import parallelai.wallet.entity.{Brand, UserSession}
import parallelai.wallet.persistence.UserSessionDAO
import scala.concurrent.duration._
import com.novus.salat.global._

import scala.concurrent.duration.Duration
import parallelai.wallet.config.ConfigConversions._

case class MongoSession( @Key("_id") sessionId: UUID, userId: UUID, applicationId: UUID, expiry: DateTime)

object MongoSession {
  implicit def mongoSessionAsUserSession(mongoSession: MongoSession): UserSession = UserSession(mongoSession.sessionId, mongoSession.userId, mongoSession.applicationId)
}

class MongoUserSessionDAO (implicit val bindingModule: BindingModule) extends UserSessionDAO with MongoConnection with Injectable {
  import MongoSession._

  val sessionTTL = injectOptionalProperty[Duration]("user.session.ttl") getOrElse 30.minutes

  val dao = new SalatDAO[MongoSession, UUID](collection = db("UserSession")) {}


  def setSessionExpiryIndex(): Unit = {
    dao.collection.ensureIndex(MongoDBObject( "expiry" -> 1, "expireAfterSeconds" -> 0))
  }

  override def validateSession(userSession: UserSession): Boolean = {
    dao.findOneById(userSession.sessionId) map {
      mongoSession =>
        mongoSessionAsUserSession(mongoSession) == userSession
    } getOrElse false
  }

  override def createNewSession(userId: UUID, applicationId: UUID): UserSession = {
    val mongoSession = MongoSession(UUID.randomUUID(), userId, applicationId, DateTime.now().plusMillis(sessionTTL.toMillis.toInt) )
    println(s"Inserting new session $mongoSession")
    dao.insert( mongoSession )
    mongoSession
  }

  override def deleteSession(sessionId: UUID): Unit = {
    dao.removeById(sessionId)
  }
}
