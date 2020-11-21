package parallelai.wallet.persistence.mongodb

import java.util.{Date, UUID}

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import parallelai.wallet.entity.{Brand, UserSession}
import parallelai.wallet.persistence.UserSessionDAO
import scala.concurrent.duration._


//import com.mongodb.BasicDBObject
import com.mongodb._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime


import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao.SalatDAO
import com.novus.salat.global._


import scala.concurrent.duration.Duration
import parallelai.wallet.config.ConfigConversions._

case class MongoSession( @Key("_id") sessionId: UUID, userId: UUID, applicationId: UUID, expiry: DateTime)

object MongoSession {
  implicit def mongoSessionAsUserSession(mongoSession: MongoSession): UserSession = UserSession(mongoSession.sessionId, mongoSession.userId, mongoSession.applicationId)
}

class MongoUserSessionDAO (implicit val bindingModule: BindingModule)
  extends UserSessionDAO
  with MongoConnection
  with Injectable {

  import MongoSession._

  val sessionTTL = injectOptionalProperty[Duration]("user.session.ttl") getOrElse 30.minutes

  private def getNextExpiry: DateTime = {
    DateTime.now().plusMillis(sessionTTL.toMillis.toInt)
  }
  
  // be sure we have session expiry correctly set
  setSessionExpiryIndex


  val dao = new SalatDAO[MongoSession, UUID](collection = db("UserSession")) {}


  def setSessionExpiryIndex(): Unit = {
    // corrected the expireAfterSeconds must be done in a 2nd MongoDBObject to be working otherwise it is
    // handled as a columnname
    dao.collection.ensureIndex(MongoDBObject( "expiry" -> 1 ), MongoDBObject("expireAfterSeconds" -> 0))
  }

  def withLog[A,R](title:String,value:A)(body : =>R): R = {
    println(s"${new Date()} entering $title with $value")
    val r=body
    println(s"${new Date()} exiting $title with $r")
    r
  }

  override def getValidSessionAndRenew(sessionId: UUID): Option[UserSession] = {
    implicit def string2UUID(o:Object) = UUID.fromString(o.toString)

    withLog("isValidSessionAndRenew",sessionId) {
      val result = dao.collection.findAndModify(
        query = MongoDBObject("_id" -> sessionId),
        update = MongoDBObject("$set" -> MongoDBObject("expiry" -> getNextExpiry))
      )
      result match {
        case None => None
        case Some(v:DBObject) => {
          Some(UserSession(sessionId=sessionId, userId= v.get("userId").toString, applicationId = v.get("applicationId").toString))
        }
      }
    }
  }


  override def isValidSession(sessionId: UUID): Boolean = {
    withLog("isValidSession",sessionId) {
      dao.findOneById(sessionId)
      match {
        case None => false
        case _ => true
      }
    }
  }



  override def createNewSession(userId: UUID, applicationId: UUID): UserSession = {
    withLog("createNewSession", (userId, applicationId)) {
      val mongoSession = MongoSession(UUID.randomUUID(), userId, applicationId, getNextExpiry)
      println(s"Inserting new session $mongoSession")
      dao.insert(mongoSession)
      mongoSession
    }
  }


  override def deleteSession(sessionId: UUID): Unit = {
    dao.removeById(sessionId)
  }
}
