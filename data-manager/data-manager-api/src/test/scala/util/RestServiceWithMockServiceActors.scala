package util

import akka.actor.ActorRef
import api.Api
import core.{BootedCore, ServiceActors}
import web.Web

// Using this trait you just need to define the service actors as TestProbe and start the rest server
class RestServiceWithMockServiceActors(
  override val messenger: ActorRef,
  override val editAccount: ActorRef,
  override val brand: ActorRef,
  override val registration: ActorRef
) extends BootedCore with ServiceActors with Api with Web
