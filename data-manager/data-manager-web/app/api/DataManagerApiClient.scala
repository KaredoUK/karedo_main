package api

import com.parallelai.wallet.datamanager.data._
import scala.concurrent.Future
import akka.actor.ActorSystem
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import ApiDataJsonProtocol._
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import spray.http.HttpResponse
import org.apache.http.HttpStatus
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import SprayJsonSupport._


trait DataManagerApiClient {

  def register(request: RegistrationRequest) : Future[RegistrationResponse]

  def validateRegistration(validation: RegistrationValidation) : Future[Boolean]
}


class DataManagerRestClient(implicit val bindingModule: BindingModule) extends DataManagerApiClient with Injectable {
  implicit val actorSystem = ActorSystem("program-info-client")

  val apiBaseUri = injectProperty[String]("data.manager.api.url")

  import actorSystem.dispatcher

  val registerPipeline = sendReceive ~> unmarshal[RegistrationResponse]
  val validatePipeline = sendReceive ~> isSuccessfulValidation

  override def register(request: RegistrationRequest): Future[RegistrationResponse] = registerPipeline { Post(apiBaseUri + "/register", request) }

  override def validateRegistration(validation: RegistrationValidation): Future[Boolean] = validatePipeline { Post(apiBaseUri + "/validateRegistration", validation) }

  private def isSuccessfulValidation(validationResponse: Future[HttpResponse]): Future[Boolean] = validationResponse map { response => response.status.isSuccess }
}

class DataManagerMockClient extends DataManagerApiClient{
  override def validateRegistration(validation: RegistrationValidation): Future[Boolean] = Future.successful(true)

  override def register(request: RegistrationRequest): Future[RegistrationResponse] =
    request match {
      case RegistrationRequest(appId, Some(msisdn), _) => Future.successful( RegistrationResponse(appId, "msisdn", msisdn) )
      case RegistrationRequest(appId, None, Some(email)) => Future.successful( RegistrationResponse(appId, "email", email) )
      case _ => Future.failed( new IllegalArgumentException("Invalid registration request, need to have at least one of msisdn or email") )
    }

}