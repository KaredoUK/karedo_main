package core

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorLogging, Actor}
import core.common.RequestValidationChaining
import parallelai.wallet.entity.{MediaContent, MediaContentDescriptor}
import parallelai.wallet.persistence.MediaDAO

object MediaContentActor {
  def props(mediaDao: MediaDAO) = Props( new MediaContentActor(mediaDao) )


  case class CreateMediaContent(content: MediaContent)
  case class GetMediaContent(id: String)
  case class FindMediaContentByName(name: String)

  case class ContentCreated(id: String)
  case class ContentSearchResponse(content: Option[MediaContent])

  sealed trait MediaHandlingError
  case object InvalidContentId extends MediaHandlingError
}

class MediaContentActor(mediaDao: MediaDAO) extends Actor with ActorLogging with RequestValidationChaining {


  override def receive: Receive = ???
}
