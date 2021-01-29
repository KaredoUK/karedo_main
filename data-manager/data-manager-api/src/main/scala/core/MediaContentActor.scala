package core

import java.io.ByteArrayInputStream

import akka.actor.Actor.Receive
import akka.actor.{Props, ActorLogging, Actor}
import com.parallelai.wallet.datamanager.data.{GetMediaResponse, GetMediaRequest, AddMediaResponse, AddMediaRequest}
import core.MediaContentActor.{InvalidContentId, MediaHandlingError}
import core.common.RequestValidationChaining
import parallelai.wallet.entity.{MediaContent, MediaContentDescriptor}
import parallelai.wallet.persistence.MediaDAO
import spray.json.{JsString, JsObject, RootJsonWriter}
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.{ Base64 => B64 }

import scala.concurrent.Future
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global

object MediaContentActor {
  //def props(mediaDao: MediaDAO) = Props( new MediaContentActor(mediaDao) )



  case class CreateMediaContent(content: MediaContent)
  case class GetMediaContent(id: String)
  case class FindMediaContentByName(name: String)

  case class ContentCreated(id: String)
  case class ContentSearchResponse(content: Option[MediaContent])

  sealed trait MediaHandlingError
  case class InvalidContentId(t: Throwable) extends MediaHandlingError
  case object MissingContent extends MediaHandlingError

  implicit object mediaErrorJsonFormat extends RootJsonWriter[MediaHandlingError] {
    def write(error: MediaHandlingError) = error match {

      case InvalidContentId(reason) => JsObject(
        "type" -> JsString("InvalidContentId"),
        "data" -> JsObject {
          "reason" -> JsString(reason.getMessage)
        }
      )

      case MissingContent => JsObject(
        "type" -> JsString("MissingContent")
      )
    }

  }
}

class MediaContentActor(implicit mediaDAO: MediaDAO) extends Actor with ActorLogging with RequestValidationChaining {


  override def receive: Receive = {
    case request: AddMediaRequest => replyToSender(addMediaRequest(request))
    case request: GetMediaRequest => replyToSender(getMediaRequest(request))
  }


  def addMediaRequest(request: AddMediaRequest): Future[ResponseWithFailure[MediaHandlingError,AddMediaResponse]] = successful {
    val descriptor = MediaContentDescriptor(name=request.name, contentType = request.contentType)
    val is = new ByteArrayInputStream(request.bytes)
    val id = mediaDAO.createNew(MediaContent(descriptor,is))
    SuccessResponse(AddMediaResponse(id))
  }

  def getMediaRequest(request: GetMediaRequest): Future[ResponseWithFailure[MediaHandlingError, Option[GetMediaResponse]]] =
    successful {
    mediaDAO.findById(request.mediaId) match {
      case Some(media) =>
        val bytes: Array[Byte] = IOUtils.toByteArray(media.inputStream)
        SuccessResponse( Some(GetMediaResponse(media.descriptor.contentType, bytes)) )

      case None => SuccessResponse(None)
    }

  }

  def replyToSender[T <: Any](response: Future[ResponseWithFailure[MediaHandlingError, T]]): Unit = {
    val replyTo = sender

    response recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InvalidContentId(t))
    } foreach {
      responseContent : ResponseWithFailure[MediaHandlingError, T] =>
        replyTo ! responseContent
    }
  }
}
