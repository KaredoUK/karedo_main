import java.util.UUID

import akka.actor.ActorSystem
import com.escalatesoft.subcut.inject.NewBindingModule._
import com.escalatesoft.subcut.inject.config.PropertiesConfigPropertySource
import core.DependencyInjection
import org.specs2.time.NoTimeConversions
import parallelai.wallet.persistence.mongodb.ClientApplicationMongoDAO

import scala.concurrent.Await._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import org.specs2.mutable.Specification



/**
 * Created by pakkio on 13/02/2015.
 */
trait ItEnvironment
  extends DependencyInjection

  with RegistrationHelpers
  with BrandHelpers
  with NoTimeConversions {

  this: Specification =>

  lazy val defaultBindingConfig =
    Map(
      "mongo.server.host" -> "127.0.0.1", //192.168.149.138",
      "mongo.server.port" -> "12345", //27017",
      "mongo.db.name" -> "wallet_data",
      "mongo.db.user" -> "",
      "mongo.db.pwd" -> ""
    )


  override implicit val bindingModule = newBindingModuleWithConfig  (
    PropertiesConfigPropertySource(defaultBindingConfig)
  )


  def responseTimeout = 30.seconds

  def wait[T](future: Future[T]): T = result(future, responseTimeout)

  implicit val system = ActorSystem()

  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  val serviceUrl = "http://localhost:8090"
  val ca=new ClientApplicationMongoDAO()
  // Random generator
  val random = new scala.util.Random

  // Generate a random string of length n from the given alphabet
  def randomString(alphabet: String)(n: Int): String =
    Stream.continually(random.nextInt(alphabet.size)).map(alphabet).take(n).mkString

  // Generate a random alphabnumeric string of length n
  def randomAlphanumericString(n: Int) =
    randomString("abcdefghijklmnopqrstuvwxyz0123456789")(n)

  def randomAlpha(n: Int) =
    randomString("abcdefghijklmnopqrstuvwxyz")(n)

  def generateMobile = {
    val randomMobile = "447700900" + f"${random.nextInt(999)}%03d"
    randomMobile
  }
  def generateEmail = {

    randomAlpha(4)+"@"+randomAlpha(4)+".it"
  }
  def notify(s:String)= {
    println(s">>> $s\n")
  }

  def isUUID(v:String) = {
    UUID.fromString(v).toString() === v
  }

}
