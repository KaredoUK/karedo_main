package parallelai.wallet.persistence.mongodb.session

import java.util.UUID

import com.escalatesoft.subcut.inject.NewBindingModule._
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.persistence.mongodb.{MongoAppSupport, MongoTestUtils}
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

class UserSessionMongoDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions
  with MongoTestUtils with MongoAppSupport {

  sequential

  "UserSessionMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> s"$embedConnectionPort",
        "mongo.db.name" -> "test",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )

    val userSessionDAO = new UserSessionMongoDAO

    "Save and retrieve session data" in {
      val userID = UUID.randomUUID()
      val sessionData = Map( "password" -> "pippo" )

      val readAfterCreate = for {
        create <- userSessionDAO.store(userID,  sessionData)
        read <- userSessionDAO.get(userID)
      } yield read

      fromFuture(readAfterCreate) shouldEqual Some(sessionData)
    }
  }

}
