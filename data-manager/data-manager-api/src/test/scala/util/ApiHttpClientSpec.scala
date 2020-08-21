package util

import akka.actor.ActorSystem
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.{Step, Fragments}
import org.specs2.time.NoTimeConversions
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import spray.testkit.TestUtils

import scala.concurrent.Await._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random


trait ApiHttpClientSpec extends SpecificationLike with NoTimeConversions with Mockito {
  // Problems acting on same mocks when running in parallel
  sequential

  def wait[T](future: Future[T]): T = result(future, 5.seconds)

  implicit val system = ActorSystem(s"${getClass.getSimpleName}ClientSystem")

  // execution context for futures

  lazy val servicePort : Int = Math.abs( Random.nextInt(2000) ) + 10000

  val serviceUrl = s"http://localhost:$servicePort"

  val mockedBrandDAO = mock[BrandDAO]
  val mockedClientApplicationDAO = mock[ClientApplicationDAO]
  val mockedUserAccountDAO = mock[UserAccountDAO]

  lazy val server = new RestServiceWithMockPersistence(servicePort, mockedBrandDAO, mockedClientApplicationDAO, mockedUserAccountDAO)

  def stopServer(): Unit = {
    println("Shutting down actor context")
    system.shutdown()
  }

  override def map(fs: =>Fragments) = Step(server) ^ fs ^ Step(stopServer())


}
