package util

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.specs2.mock.Mockito
import org.specs2.mutable.{After, Specification}
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{ClientApplication, UserSession}
import parallelai.wallet.persistence._
import spray.http.HttpHeaders.RawHeader

import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random


trait ActorsSpec
  extends Specification
  with NoTimeConversions
  with Mockito
{
  def responseTimeout = 5.seconds

  val  sessionId = UUID.randomUUID()
  val userId = UUID.randomUUID()
  val applicationId = UUID.randomUUID()


  trait WithMockedPersistence extends After {

    implicit val system = ActorSystem(s"${getClass.getSimpleName}ClientSystem".replace('$', 'S'))

    def wait[T](future: Future[T]): T = result(future, responseTimeout)

    lazy val mockedBrandDAO = mock[BrandDAO]
    lazy val mockedLogDAO = mock[LogDAO]
    lazy val mockedHintDAO = mock[HintDAO]
    lazy val mockedClientApplicationDAO = mock[ClientApplicationDAO]
    lazy val mockedUserAccountDAO = mock[UserAccountDAO]
    lazy val mockedMediaDAO = mock[MediaDAO]
    lazy val mockedOfferDAO = mock[OfferDAO]
    lazy val mockedUserSessionDAO = mock[UserSessionDAO]
    lazy val messagerActor = TestProbe()
    lazy val mockedSaleDAO = mock[SaleDAO]


    mockedUserSessionDAO.getValidSessionAndRenew(sessionId) returns
      Some(UserSession(sessionId, userId, applicationId))

    mockedClientApplicationDAO.findByUserId(userId) returns
      Seq(ClientApplication(applicationId, userId, "xxxx", active= true))

    val server = new WithMockPersistence(
      8080, mockedBrandDAO, mockedHintDAO, mockedLogDAO,
      mockedClientApplicationDAO, mockedUserAccountDAO,
      mockedMediaDAO, mockedOfferDAO, mockedUserSessionDAO, mockedSaleDAO,
      messagerActor.ref)


    def after = stopServer()

    def stopServer(): Unit = {
      println("Shutting down actor context")
      system.shutdown()
    }
  }
}
