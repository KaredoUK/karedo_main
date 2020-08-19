package core

import java.util.UUID

import akka.actor.{ActorRef, ActorDSL, Props, ActorSystem}
import ActorDSL._
import akka.routing.{RoundRobinPool, RoundRobinRouter}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import parallelai.wallet.entity.Brand
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import parallelai.wallet.config.AppConfigPropertySource
import com.typesafe.config.ConfigFactory
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.persistence.mongodb.{MongoBrandDAO, ClientApplicationMongoDAO, UserAccountMongoDAO}

import scala.concurrent.Future

/**
 * Core is type containing the ``system: ActorSystem`` member. This enables us to use it in our
 * apps as well as in our tests.
 */
trait Core {

  implicit def system: ActorSystem

}

/**
 * This trait implements ``Core`` by starting the required ``ActorSystem`` and registering the
 * termination handler to stop the system when the JVM exits.
 */
trait BootedCore extends Core {

  /**
   * Construct the ActorSystem we will use in our application
   */
  implicit lazy val system = ActorSystem("akka-spray")

  /**
   * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
   */
  sys.addShutdownHook(system.shutdown())

}

trait DependencyInjection extends Injectable {
  implicit val configProvider = AppConfigPropertySource(ConfigFactory.load())
  override implicit val bindingModule : BindingModule = newBindingModuleWithConfig
}

trait Persistence {
  def brandDAO : BrandDAO  
  def userAccountDAO : UserAccountDAO
  def clientApplicationDAO : ClientApplicationDAO
}

trait MongoPersistence extends Persistence {
  self : Injectable =>
  
  override val userAccountDAO : UserAccountDAO = new UserAccountMongoDAO() 
  override val brandDAO : BrandDAO = new MongoBrandDAO()
  override val clientApplicationDAO : ClientApplicationDAO = new ClientApplicationMongoDAO()
}

trait ServiceActors {
  def messenger: ActorRef
  def registration: ActorRef
  def brand: ActorRef
  def editAccount: ActorRef
}

/**
 * This trait contains the actors that make up our application; it can be mixed in with
 * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
 */
trait CoreActors extends ServiceActors {
  this: Core with Persistence with Injectable =>

  val emailActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.email") getOrElse 2
  val smsActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.sms") getOrElse 2
  val brandActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.brand") getOrElse 3
  val registrationActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.registration") getOrElse 3
  val editAccountActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.editAccount") getOrElse 3

  val emailActor = system.actorOf(EmailActor.props.withRouter( RoundRobinPool(nrOfInstances = emailActorPoolSize) ) )
  val smsActor = system.actorOf(SMSActor.props .withRouter( RoundRobinPool(nrOfInstances = smsActorPoolSize) ) )

  override val messenger = system.actorOf(MessengerActor.props(emailActor, smsActor))

  // This should be an actor pool at least if we don't want to use a one actor per request strategy
  override val registration = system.actorOf(
    RegistrationActor.props(userAccountDAO, clientApplicationDAO, messenger)
      .withRouter( RoundRobinPool(nrOfInstances = registrationActorPoolSize) )
  )
  override val brand = system.actorOf(
    BrandActor.props(brandDAO)
      .withRouter( RoundRobinPool(nrOfInstances = brandActorPoolSize) )
  )

  override val editAccount = system.actorOf(
    EditAccountActor.props(userAccountDAO, clientApplicationDAO, brandDAO)
      .withRouter( RoundRobinPool(nrOfInstances = editAccountActorPoolSize) )
  )
}