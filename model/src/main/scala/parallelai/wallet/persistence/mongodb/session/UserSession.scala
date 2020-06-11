package parallelai.wallet.persistence.mongodb.session

import java.util.UUID

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.MongoDBObjectBuilder
import com.mongodb.casbah.commons.ValidBSONType.DBObject
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.novus.salat.global._
import com.novus.salat.dao._

import org.joda.time.DateTime
import parallelai.wallet.persistence.ClientApplicationDAO
import parallelai.wallet.persistence.mongodb.{MongoConnection, MongoUserAccount}
import parallelai.wallet.persistence.session._
import scala.concurrent.Future._

import scala.concurrent.Future

case class UserSession(@Key("_id")userId: UUID, sessionData: UserSessionData, updatedAt: DateTime)

class UserSessionMongoDAO(implicit val bindingModule: BindingModule)  extends UserSessionDAO with MongoConnection with Injectable {
  lazy val mongoHost: String = injectProperty[String]("mongo.server.host")
  lazy val mongoPort: Int = injectProperty[Int]("mongo.server.port")
  lazy val mongoDbName: String = injectProperty[String]("mongo.db.name")
  lazy val mongoDbUser: String = injectProperty[String]("mongo.db.user")
  lazy val mongoDbPwd: String = injectProperty[String]("mongo.db.pwd")

  val dao = new SalatDAO[UserSession, UUID](collection = db("UserSession")) {}

  override def get(userId: UUID): Future[Option[UserSessionData]] =
    successful {
      val partResult = dao.collection.findOneByID(userId.asInstanceOf[AnyRef]).map { _.get("sessionData") }
      println(partResult)
      Some(Map.empty[String, String])
    }


  def asMongoMap(map: Map[String, String]): MongoDBObject = {
    val resultBuilder = map.foldLeft(new MongoDBObjectBuilder()) { (builder, entry) => builder += (entry._1 -> entry._2) }
    resultBuilder.result
  }

  override def store(userId: UUID, userSession: UserSessionData): Future[Unit] =
    successful {
      dao.update(
        MongoDBObject("_id" -> userId),
        $set(
          "sessionData" -> asMongoMap(userSession),
          "updatedAt" -> DateTime.now
        ),
        upsert = true
      )
    }
}
