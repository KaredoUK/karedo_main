package core

import java.util.UUID

import akka.actor._
import ActorDSL._
import akka.routing.{RoundRobinGroup, RoundRobinPool, RoundRobinRouter}
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import core.security.{UserAuthServiceImpl, UserAuthService}
import parallelai.wallet.entity.Brand
import parallelai.wallet.entity.api.offermanager.RetailOffer
import parallelai.wallet.persistence._
import parallelai.wallet.config.AppConfigPropertySource
import com.typesafe.config.ConfigFactory
import com.escalatesoft.subcut.inject.NewBindingModule._
import parallelai.wallet.persistence.mongodb._

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
  implicit val configProvider = {
    println(java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments)
    println(s"\n**** config.resource: ${System.getProperty("config.resource")}")
    println(  s"**** config.file: ${System.getProperty("config.file")}\n")
    AppConfigPropertySource( ConfigFactory.load().withFallback(ConfigFactory.parseResources("application.default.conf")) )
  }
  override implicit val bindingModule : BindingModule = newBindingModuleWithConfig
}

trait Persistence {
  def brandDAO : BrandDAO
  def hintDAO : HintDAO
  def offerDAO : OfferDAO
  def mediaDAO : MediaDAO
  def userAccountDAO : UserAccountDAO
  def clientApplicationDAO : ClientApplicationDAO
  def userSessionDAO: UserSessionDAO
}

trait MongoPersistence extends Persistence {
  self : Injectable =>
  
  override val userAccountDAO : UserAccountDAO = new UserAccountMongoDAO() 
  override val brandDAO : BrandDAO = new BrandMongoDAO()
  override val hintDAO : HintDAO = new HintMongoDAO()
  override val offerDAO : OfferDAO = new OfferMongoDAO()
  override val mediaDAO : MediaDAO = new MongoMediaDAO()
  override val clientApplicationDAO : ClientApplicationDAO = new ClientApplicationMongoDAO()
  override val userSessionDAO: UserSessionDAO = new MongoUserSessionDAO()
}

trait ServiceActors {
  def registration: ActorRef
  def brand: ActorRef
  def offer: ActorRef
  def other: ActorRef
  def media: ActorRef
  def editAccount: ActorRef
  def userAuthentication: UserAuthService
}

trait MessageActors {
  def messenger: ActorRef
}

trait RestMessageActors extends MessageActors {
  this: Core with Persistence with Injectable =>

  val emailActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.email") getOrElse 2
  val smsActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.sms") getOrElse 2
  val smsActorClassName = injectOptionalProperty[String]("notification.sms.actor.class") getOrElse classOf[SMSActor].getName
  val emailActorClassName = injectOptionalProperty[String]("notification.email.actor.class") getOrElse classOf[EmailActor].getName

  def emailActorProps = Props( Class.forName(emailActorClassName), bindingModule)
  val emailActor = system.actorOf(emailActorProps.withRouter( RoundRobinPool(nrOfInstances = emailActorPoolSize) ) )

  def smsActorProps = Props( Class.forName(smsActorClassName), bindingModule )
  val smsActor = system.actorOf( smsActorProps.withRouter( RoundRobinPool(nrOfInstances = smsActorPoolSize) ) )

  override val messenger = system.actorOf(MessengerActor.props(emailActor, smsActor))
}

/**
 * This trait contains the actors that make up our application; it can be mixed in with
 * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
 */
trait BaseCoreActors extends ServiceActors with RestMessageActors  {
  this: Core with Persistence with Injectable with MessageActors =>

  val mediaActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.media") getOrElse 3
  val brandActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.brand") getOrElse 3
  val offerActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.offer") getOrElse 3
  val otherActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.other") getOrElse 3
  val registrationActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.registration") getOrElse 3
  val editAccountActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.editAccount") getOrElse 3
  val userAuthActorPoolSize = injectOptionalProperty[Int]("actor.pool.size.userAuthentication") getOrElse 5

  override val registration = system.actorOf(
    RegistrationActor.props(userAccountDAO, clientApplicationDAO, userSessionDAO, messenger)
      .withRouter( RoundRobinPool(nrOfInstances = registrationActorPoolSize) ),
    "Registration"
  )

  override val brand = system.actorOf(
    BrandActor.props(brandDAO, hintDAO)
      .withRouter( RoundRobinPool(nrOfInstances = brandActorPoolSize) ),
    "Brand"
  )

  override val media = system.actorOf(
    MediaContentActor.props(mediaDAO)
      .withRouter( RoundRobinPool(nrOfInstances = mediaActorPoolSize) ),
    "Media"
  )

  override val offer = system.actorOf(
    OfferActor.props(offerDAO)
      .withRouter( RoundRobinPool(nrOfInstances = offerActorPoolSize) ),
    "Offer"
  )

  override val other = system.actorOf(
    OtherActor.props()
      .withRouter( RoundRobinPool(nrOfInstances = otherActorPoolSize) ),
    "Other"
  )

  override val editAccount = system.actorOf(
    EditAccountActor.props(userAccountDAO, clientApplicationDAO, brandDAO)
      .withRouter( RoundRobinPool(nrOfInstances = editAccountActorPoolSize) ),
    "EditAccount"
  )

  override val userAuthentication: UserAuthService = createUserAuthenticationPool(userAuthActorPoolSize)

  def createUserAuthenticationPool(poolSize: Int): UserAuthService = {
    // see http://doc.akka.io/docs/akka/snapshot/scala/typed-actors.html

    val userAuthActors = List.range(0, poolSize).map { idx =>
      TypedActor(system).typedActorOf(
        TypedProps(classOf[UserAuthService], new UserAuthServiceImpl(userSessionDAO, clientApplicationDAO)), s"userAuthenticationActor-$idx"
      )
    }
    val userAuthActorsPaths = userAuthActors map { r =>
      TypedActor(system).getActorRefFor(r).path.toStringWithoutAddress
    }
    val userAuthActorsRouter: ActorRef = system.actorOf(RoundRobinGroup(userAuthActorsPaths).props())

    TypedActor(system).typedActorOf(TypedProps[UserAuthService](), actorRef = userAuthActorsRouter)
  }
}

trait CoreActors extends BaseCoreActors with RestMessageActors {
  this: Core with Persistence with Injectable =>
}