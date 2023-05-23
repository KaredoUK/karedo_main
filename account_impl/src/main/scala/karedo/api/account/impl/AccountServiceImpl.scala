package karedo.api.account.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import karedo.api.account
import karedo.api.account.{AccountService, RegisterRequest}

/**
  * Implementation of the SotService.
  */
class AccountServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends AccountService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the SoT entity for the given ID.
    val ref = persistentEntityRegistry.refFor[AccountEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id))
  }

  override def useGreeting(id: String) = ServiceCall { request =>
    // Look up the SoT entity for the given ID.
    val ref = persistentEntityRegistry.refFor[AccountEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message))
  }

  override def register() = ServiceCall { request =>
    val ref = persistentEntityRegistry.refFor[AccountEntity](request.application_id)

    ref.ask(UseGreetingMessage(request.email))
  }


  override def greetingsTopic(): Topic[account.GreetingMessageChanged] =
    TopicProducer.singleStreamWithOffset {
      fromOffset =>
        persistentEntityRegistry.eventStream(AccountEvent.Tag, fromOffset)
          .map(ev => (convertEvent(ev), ev.offset))
    }

  private def convertEvent(helloEvent: EventStreamElement[AccountEvent]): account.GreetingMessageChanged = {
    helloEvent.event match {
      case GreetingMessageChanged(msg) => account.GreetingMessageChanged(helloEvent.entityId, msg)
    }
  }
}
