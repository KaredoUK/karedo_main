package karedo.api.account.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import karedo.api.account.{GreetingMessage, AccountService}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import parallelai.sot.api._

class AccountServiceSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  private val server = ServiceTest.startServer(
    ServiceTest.defaultSetup
      .withCassandra(true)
  ) { ctx =>
    new AccountApplication(ctx) with LocalServiceLocator
  }

  val client = server.serviceClient.implement[AccountService]

  override protected def afterAll() = server.stop()

  "SoT service" should {

    "say hello" in {
      client.hello("Alice").invoke().map { answer =>
        answer should ===("Hello, Alice!")
      }
    }

    "allow responding with a custom message" in {
      for {
        _ <- client.useGreeting("Bob").invoke(GreetingMessage("Bob", "Hi"))
        answer <- client.hello("Bob").invoke()
      } yield {
        answer should ===("Hi, Bob!")
      }
    }
  }
}
