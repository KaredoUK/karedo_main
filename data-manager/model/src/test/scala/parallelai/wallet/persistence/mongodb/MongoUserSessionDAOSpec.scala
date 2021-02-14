package parallelai.wallet.persistence.mongodb

import java.util.UUID._
import com.escalatesoft.subcut.inject.config.PropertiesConfigPropertySource
import com.mongodb.casbah.commons.MongoDBObject
import org.specs2.time.NoTimeConversions
import scala.concurrent.duration._
import org.specs2.mutable.{Before, Specification}
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.entity.UserSession
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MongoUserSessionDAOSpec
  extends Specification
  with MongoTestUtils
  with NoTimeConversions
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
    userSessionDAO.dao.collection.remove(MongoDBObject())
    println(s"Setting expiry index for user session")
    userSessionDAO.setSessionExpiryIndex()
    println("...done")
  }

  sequential

  "A MongoUserSessionDAO" should {
    "create a new valid session" in {
      println("((((TEST1 BEGIN))))")
      val userId = randomUUID()
      val appId = randomUUID()
      val session = userSessionDAO.createNewSession(userId, appId)

      userSessionDAO.isValidSession(session.sessionId) must beTrue
    }

    "Not validate not existing session" in {
      println("((((TEST2 BEGIN))))")
      userSessionDAO.isValidSession(randomUUID()) must beFalse
    }

    "Not validate deleted session" in {
      println("((((TEST3 BEGIN))))")
      val userId = randomUUID()
      val appId = randomUUID()
      val session = userSessionDAO.createNewSession(userId, appId)

      userSessionDAO.deleteSession(session.sessionId)

      userSessionDAO.isValidSession(session.sessionId) must beFalse
    }

/*
    "Be able to extend a session if using it (this takes around 3 minutes)" in {
      println("((((TEST5 BEGIN))))")

      implicit val bindingModule =
        newBindingModuleWithConfig(
          PropertiesConfigPropertySource(
            defaultBindingConfig +  ( "user.session.ttl" -> "15.seconds" )
          )
        )

      val quickExpirySessionDAO = new MongoUserSessionDAO()

      val userId = randomUUID()
      val appId = randomUUID()
      val session = quickExpirySessionDAO.createNewSession(userId, appId)


      // we must be able to work for more than one minute
      for(i <- 1 until 8) {
        Thread sleep 10.seconds.toMillis
        quickExpirySessionDAO.getValidSessionAndRenew(session.sessionId) must beSome[UserSession]
      }

      // but should go away within 80 seconds...
      quickExpirySessionDAO.isValidSession(session.sessionId) must beFalse.eventually(20,4.seconds)

    }

    "Not validate expired session" in {
      println("((((TEST4 BEGIN))))")

      implicit val bindingModule =
      newBindingModuleWithConfig(
          PropertiesConfigPropertySource(
              defaultBindingConfig +  ( "user.session.ttl" -> "2.seconds" )
          )
      )

      val quickExpirySessionDAO = new MongoUserSessionDAO()

      val userId = randomUUID()
      val appId = randomUUID()
      val session = quickExpirySessionDAO.createNewSession(userId, appId)

      // MongoDB TTL policy is using a thread running every 60 seconds so we probably must
      // wait 60 seconds + few others
      userSessionDAO.isValidSession(session.sessionId) must beFalse.eventually(20, 4.seconds)
    }

*/
  }

}
