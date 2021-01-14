import java.util.UUID

import akka.actor.ActorSystem
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
trait MyUtility
  extends DependencyInjection with NoTimeConversions {
  this: Testing =>



  def responseTimeout = 30.seconds

  def wait[T](future: Future[T]): T = result(future, responseTimeout)

  implicit val system = ActorSystem()

  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  val serviceUrl = "http://localhost:8090"
  val ca=new ClientApplicationMongoDAO()

  def generateMobile = {
    val random = new java.util.Random
    val randomMobile = "447700900" + f"${random.nextInt(999)}%03d"
    randomMobile
  }
  def notify(s:String)= {
    println(s">>> $s\n")
  }

  def isUUID(v:String) = {
    UUID.fromString(v).toString() === v
  }

}
