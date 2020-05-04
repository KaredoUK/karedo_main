package core

import akka.actor.{ActorDSL, Props, ActorSystem}
import ActorDSL._
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import parallelai.wallet.config.AppConfigPropertySource
import com.typesafe.config.ConfigFactory
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}

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

/**
 * This trait contains the actors that make up our application; it can be mixed in with
 * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
 */
trait CoreActors {
  this: Core =>

  implicit val configProvider = AppConfigPropertySource(ConfigFactory.load())

  implicit val bindingModule = newBindingModuleWithConfig

  val userAccountDAO : UserAccountDAO = new UserAccountMongoDAO()

  val emailActor = system.actorOf(EmailActor.props)
  val smsActor = system.actorOf(SMSActor.props)

  val messenger = system.actorOf(MessengerActor.props(emailActor, smsActor))

  val clientApplicationDAO : ClientApplicationDAO = new ClientApplicationMongoDAO()
  // This should be an actor pool at least if we don't want to use a one actor per request strategy
  val registration = system.actorOf(RegistrationActor.props(userAccountDAO, clientApplicationDAO, messenger))

  val editAccount = system.actorOf(EditAccountActor.props(userAccountDAO, clientApplicationDAO))
}