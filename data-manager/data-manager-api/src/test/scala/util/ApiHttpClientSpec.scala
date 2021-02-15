package util

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import org.specs2.mock.Mockito
import org.specs2.mutable.{After, SpecificationLike}
import org.specs2.specification.{Step, Fragments}
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{ClientApplication, UserSession}
import parallelai.wallet.persistence._
import spray.http.HttpHeaders.RawHeader
import spray.testkit.TestUtils
import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random
import org.specs2.mutable.Specification


trait ApiHttpClientSpec
  extends Specification
  with NoTimeConversions
  with Mockito
{
  def responseTimeout = 5.seconds

  val  sessionId = UUID.randomUUID()
  val userId = UUID.randomUUID()
  val applicationId = UUID.randomUUID()

  val  headers = List(RawHeader("X-Session-Id",sessionId.toString()))

  trait WithMockedPersistenceRestService extends After {



    val servicePort : Int = Math.abs( Random.nextInt(3000) ) + 10000
    val serviceUrl = s"http://localhost:$servicePort"

    implicit val system = ActorSystem(s"${getClass.getSimpleName}ClientSystem".replace('$', 'S'))

    def wait[T](future: Future[T]): T = result(future, responseTimeout)

    lazy val mockedBrandDAO = mock[BrandDAO]
    lazy val mockedHintDAO = mock[HintDAO]
    lazy val mockedLogDAO = mock[LogDAO]
    lazy val mockedClientApplicationDAO = mock[ClientApplicationDAO]
    lazy val mockedUserAccountDAO = mock[UserAccountDAO]
    lazy val mockedMediaDAO = mock[MediaDAO]
    lazy val mockedOfferDAO = mock[OfferDAO]
    lazy val mockedUserSessionDAO = mock[UserSessionDAO]
    lazy val mockedSaleDAO = mock[KaredoSalesDAO]
    lazy val messagerActor = TestProbe()

    mockedUserSessionDAO.getValidSessionAndRenew(sessionId) returns
      Some(UserSession(sessionId, userId, applicationId))

    mockedClientApplicationDAO.findByUserId(userId) returns
      Seq(ClientApplication(applicationId, userId, "xxxx", active= true))

    val server = new RestServiceWithMockPersistence(
      servicePort, mockedBrandDAO, mockedHintDAO, mockedLogDAO,
      mockedClientApplicationDAO, mockedUserAccountDAO,
      mockedMediaDAO, mockedOfferDAO, mockedUserSessionDAO, mockedSaleDAO,
      messagerActor.ref)

    /*println("Sleeping 2 seconds to wait for the httpserver to start")
    Thread.sleep(2000)*/

    def after = stopServer()

    def stopServer(): Unit = {
      println("Shutting down actor context")
      system.shutdown()
    }
  }
}
