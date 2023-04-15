package services

import javax.inject._

import akka.actor._
import jobs.AdLoadActor
import play.api.inject._
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by charaj on 12/02/2017.
  */
@Singleton
class FeedLoaderService @Inject() (appLifecycle: ApplicationLifecycle, actorSystem: ActorSystem) {
  println("Launching FeedLoaderService")

  actorSystem.scheduler.schedule(0 seconds, 1 hour, actorSystem.actorOf(Props[AdLoadActor], "AdLoadActor"), "load")

}
