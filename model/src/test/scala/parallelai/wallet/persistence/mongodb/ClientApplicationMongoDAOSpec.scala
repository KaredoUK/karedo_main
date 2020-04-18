package parallelai.wallet.persistence.mongodb

import org.specs2.mutable.Specification
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import org.specs2.time.NoTimeConversions
import com.escalatesoft.subcut.inject.NewBindingModule._
import scala.concurrent.ExecutionContext.Implicits.global
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.entity.{ClientApplication, UserAccount}
import java.util.UUID

class ClientApplicationMongoDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions with MongoTestUtils{
  sequential

  "ClientApplicationMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> s"$embedConnectionPort",
        "mongo.db.name" -> "test"
      )
    )

    val accountDAO = new UserAccountMongoDAO
    val clientAppDAO = new ClientApplicationMongoDAO

    val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), Some("user@email.com"))
    val clientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")


    "Find a user's application" in {

      val findAfterInsert =
        for {
          createNew <- accountDAO.insertNew(userAccount, clientApplication)
          getAppById <- clientAppDAO.getById(clientApplication.id)
        } yield getAppById

      fromFuture(findAfterInsert) shouldEqual Some(clientApplication)
    }

  }

}
