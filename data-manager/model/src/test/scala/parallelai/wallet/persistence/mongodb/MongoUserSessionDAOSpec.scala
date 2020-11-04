package parallelai.wallet.persistence.mongodb

import java.util.UUID._
import com.escalatesoft.subcut.inject.config.PropertiesConfigPropertySource

import scala.concurrent.duration._
import org.specs2.mutable.{Before, Specification}
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.entity.UserSession

class MongoUserSessionDAOSpec
  extends Specification
  with TestWithLocalMongoDb
  with Before
{
  lazy val userSessionDAO = {
    implicit val bindingModule =
      newBindingModuleWithConfig(
        PropertiesConfigPropertySource(
          defaultBindingConfig +  ( "user.session.ttl" -> "10.minutes" )
        )
      )

    new MongoUserSessionDAO()
  }

  def before = {
    clearAll()
    println(s"Setting expiry index for user session")
    userSessionDAO.setSessionExpiryIndex()
    println("...done")
  }

  sequential

  "A MongoUserSessionDAO" should {
    "create a new valid session" in {
      val userId = randomUUID()
      val appId = randomUUID()
      val session = userSessionDAO.createNewSession(userId, appId)

      userSessionDAO.validateSession(session) shouldEqual true
    }

    "Not validate not existing session" in {
      userSessionDAO.validateSession(UserSession(randomUUID(), randomUUID(), randomUUID())) shouldEqual false
    }

    "Not validate deleted session" in {
      val userId = randomUUID()
      val appId = randomUUID()
      val session = userSessionDAO.createNewSession(userId, appId)

      userSessionDAO.deleteSession(session.sessionId)

      userSessionDAO.validateSession(session) shouldEqual false
    }

    // TO BE FIXED
//    "Not validate expired session" in {
//
//      implicit val bindingModule =
//        newBindingModuleWithConfig(
//          PropertiesConfigPropertySource(
//            defaultBindingConfig +  ( "user.session.ttl" -> "2.seconds" )
//          )
//        )
//
//      val quickExpirySessionDAO = new MongoUserSessionDAO()
//
//      val userId = randomUUID()
//      val appId = randomUUID()
//      val session = quickExpirySessionDAO.createNewSession(userId, appId)
//
//      //Expiry is handled by mongo every 60 seconds
//      Thread.sleep( 60.seconds.toMillis )
//
//      userSessionDAO.validateSession(session) shouldEqual false
//    }

  }
}
