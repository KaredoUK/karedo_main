package parallelai.wallet.persistence.mongodb

import org.specs2.mutable.Specification
import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import com.escalatesoft.subcut.inject.NewBindingModule
import NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigMapSource
import parallelai.wallet.entity.{ClientApplication, UserInfo, UserAccount}
import java.util.UUID
import scala.concurrent.{Future, Await}
import Await._
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class UserAccountMongoDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions {

  sequential

  implicit def mapAsConfigSource : Map[String, String] => PropertiesConfigMapSource = new PropertiesConfigMapSource(_)

  def fromFuture[T] (future: Future[T]) : T = result(future, 2.seconds)

  "UserAccountMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> s"$embedConnectionPort",
        "mongo.db.name" -> "test"
      )
    )

    val accountDAO = new UserAccountMongoDAO

    val userAccount = UserAccount(UUID.randomUUID(), Some("12345678"), None)
    val clientApplication = ClientApplication(UUID.randomUUID(), userAccount.id, "ACT_CODE")

    "Save and retreive a user account" in {
      val readAfterInsert =
           for {
             insert <- accountDAO.insertNew(userAccount, clientApplication)
             read <- accountDAO.getById(userAccount.id)
           } yield read


      fromFuture(readAfterInsert) shouldEqual Some(userAccount)

    }


    "Find account by application ID" in {
      val findAfterInsert =
        for {
          insert <- accountDAO.insertNew(userAccount, clientApplication)
          read <- accountDAO.getByApplicationId(clientApplication.id)
        } yield read

      fromFuture(findAfterInsert) shouldEqual Some(userAccount)
    }
  }

}
