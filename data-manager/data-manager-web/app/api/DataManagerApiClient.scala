package api

import com.parallelai.wallet.datamanager.data._
import scala.concurrent.Future
import akka.actor.ActorSystem
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import ApiDataJsonProtocol._
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import spray.http._
import org.apache.http.HttpStatus
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import SprayJsonSupport._
import java.util.UUID
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import spray.http.HttpResponse
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.UserProfile
import com.parallelai.wallet.datamanager.data.RegistrationRequest


trait DataManagerApiClient {
  def getUserProfile(accountId: UUID) : Future[Option[UserProfile]]
  def findUser(msisdn: Option[String], email: Option[String]) : Future[Option[UserProfile]]

  def register(request: RegistrationRequest) : Future[RegistrationResponse]
  def addApplication(accountId: UUID, applicationId: ApplicationID) : Future[RegistrationResponse]

  def validateRegistration(validation: RegistrationValidation) : Future[RegistrationValidationResponse]
}


class DataManagerRestClient(implicit val bindingModule: BindingModule) extends DataManagerApiClient with Injectable {
  implicit val actorSystem = ActorSystem("program-info-client")

  val apiBaseUri = injectOptionalProperty[String]("data.manager.api.url") getOrElse "http://localhost:8080"

  import actorSystem.dispatcher

  val registerPipeline = sendReceive ~> unmarshal[RegistrationResponse]
  val validatePipeline = sendReceive ~> unmarshal[RegistrationValidationResponse]
  val retrieveUserProfilePipeline = sendReceive ~> notFoundToNone ~> unmarshal[Option[UserProfile]]

  override def register(request: RegistrationRequest): Future[RegistrationResponse] = registerPipeline { Post(apiBaseUri + "/account", request) }

  override def addApplication(accountId: UUID, applicationId: ApplicationID) : Future[RegistrationResponse] = registerPipeline { Put(apiBaseUri + s"/$accountId/application/$applicationId") }

  override def validateRegistration(validation: RegistrationValidation): Future[RegistrationValidationResponse] = validatePipeline { Post(apiBaseUri + "/account/application/validation", validation) }

  override def getUserProfile(accountId: UUID) : Future[Option[UserProfile]] = retrieveUserProfilePipeline { Get(apiBaseUri + s"/account/$accountId")}

  override def findUser(msisdnOp: Option[String], emailOp: Option[String]): Future[Option[UserProfile]] = {
    val findBy = msisdnOp map { msisdn => s"msisdn=$msisdn" } orElse ( emailOp map { email => s"email=$email" } )

    findBy match {
      case Some(query) => retrieveUserProfilePipeline { Get(apiBaseUri + s"/account?$query") }
      case None => Future.failed(new IllegalArgumentException("Invalid identification, need to provide at least one of msisdn and email"))
    }
  }

  def notFoundToNone(response: HttpResponse): HttpResponse = {
    if(response.status == StatusCodes.NotFound) {
      response.copy(
        status = StatusCodes.OK,
        entity = HttpEntity(ContentTypes.`application/json`, "")
      )
    } else {
      response
    }
  }
}
