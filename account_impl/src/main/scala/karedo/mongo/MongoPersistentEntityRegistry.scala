package karedo.api.account.lagom.mongo

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.event.Logging
import akka.persistence.query.Offset
import akka.stream.scaladsl
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

final class MongoPersistentEntityRegistry(system: ActorSystem)
  extends PersistentEntityRegistry
{

  private val log = Logging.getLogger(system, getClass)

//  override protected val journalId: String = "mongo-persistence-journal"
  /**
    * At system startup all [[com.lightbend.lagom.scaladsl.persistence.PersistentEntity]]
    * classes must be registered with this method.
    *
    * The `entityFactory` will be called when a new entity instance is to be created.
    * That will happen in another thread, so the `entityFactory` must be thread-safe, e.g.
    * not close over shared mutable state that is not thread-safe.
    */
  override def register(entityFactory: => PersistentEntity): Unit = ???

  /**
    * Retrieve a [[com.lightbend.lagom.scaladsl.persistence.PersistentEntityRef]] for a
    * given [[com.lightbend.lagom.scaladsl.persistence.PersistentEntity]] class
    * and identifier. Commands are sent to a `PersistentEntity` using a `PersistentEntityRef`.
    */
  override def refFor[P <: PersistentEntity : ClassManifest](entityId: String): PersistentEntityRef[P#Command] = ???

  /**
    * A stream of the persistent events that have the given `aggregateTag`, e.g.
    * all persistent events of all `Order` entities.
    *
    * The type of the offset is journal dependent, some journals use time-based
    * UUID offsets, while others use sequence numbers. The passed in `fromOffset`
    * must either be [[akka.persistence.query.NoOffset]], or an offset that has previously been produced
    * by this journal.
    *
    * The stream will begin with events starting ''after'' `fromOffset`.
    * To resume an event stream, store the `Offset` corresponding to the most
    * recently processed `Event`, and pass that back as the value for
    * `fromOffset` to start the stream from events following that one.
    *
    * @throws IllegalArgumentException If the `fromOffset` type is not supported
    *                                  by this journal.
    */
  override def eventStream[Event <: AggregateEvent[Event]](aggregateTag: AggregateEventTag[Event], fromOffset: Offset): Source[EventStreamElement[Event], NotUsed] = ???

  /**
    * Gracefully stop the persistent entities and leave the cluster.
    * The persistent entities will be started on another node when
    * new messages are sent to them.
    *
    * @return the `Future` is completed when the node has been
    *         removed from the cluster
    */
  override def gracefulShutdown(timeout: FiniteDuration): Future[Done] = ???
}

/**
  * At system startup all [[PersistentEntity]] classes must be registered here
  * with [[PersistentEntityRegistry#register]].
  *
  * Later, [[com.lightbend.lagom.scaladsl.persistence.PersistentEntityRef]] can be
  * retrieved with [[PersistentEntityRegistry#refFor]].
  * Commands are sent to a [[com.lightbend.lagom.scaladsl.persistence.PersistentEntity]]
  * using a `PersistentEntityRef`.
  */
trait PersistentEntityRegistry {

  /**
    * At system startup all [[com.lightbend.lagom.scaladsl.persistence.PersistentEntity]]
    * classes must be registered with this method.
    *
    * The `entityFactory` will be called when a new entity instance is to be created.
    * That will happen in another thread, so the `entityFactory` must be thread-safe, e.g.
    * not close over shared mutable state that is not thread-safe.
    */
  def register(entityFactory: => PersistentEntity): Unit

  /**
    * Retrieve a [[com.lightbend.lagom.scaladsl.persistence.PersistentEntityRef]] for a
    * given [[com.lightbend.lagom.scaladsl.persistence.PersistentEntity]] class
    * and identifier. Commands are sent to a `PersistentEntity` using a `PersistentEntityRef`.
    */
  def refFor[P <: PersistentEntity: ClassTag](entityId: String): PersistentEntityRef[P#Command]

  /**
    * A stream of the persistent events that have the given `aggregateTag`, e.g.
    * all persistent events of all `Order` entities.
    *
    * The type of the offset is journal dependent, some journals use time-based
    * UUID offsets, while others use sequence numbers. The passed in `fromOffset`
    * must either be [[akka.persistence.query.NoOffset]], or an offset that has previously been produced
    * by this journal.
    *
    * The stream will begin with events starting ''after'' `fromOffset`.
    * To resume an event stream, store the `Offset` corresponding to the most
    * recently processed `Event`, and pass that back as the value for
    * `fromOffset` to start the stream from events following that one.
    *
    * @throws IllegalArgumentException If the `fromOffset` type is not supported
    *   by this journal.
    */
  def eventStream[Event <: AggregateEvent[Event]](
                                                   aggregateTag: AggregateEventTag[Event],
                                                   fromOffset:   Offset
                                                 ): scaladsl.Source[EventStreamElement[Event], NotUsed]

  /**
    * Gracefully stop the persistent entities and leave the cluster.
    * The persistent entities will be started on another node when
    * new messages are sent to them.
    *
    * @return the `Future` is completed when the node has been
    *   removed from the cluster
    */
  def gracefulShutdown(timeout: FiniteDuration): Future[Done]

}

