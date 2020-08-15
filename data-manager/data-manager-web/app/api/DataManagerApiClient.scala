package api

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.util.SprayJsonSupport
import scala.concurrent.Future
import akka.actor.ActorSystem
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import com.parallelai.wallet.datamanager.data.RegistrationRequest
import ApiDataJsonProtocol._
import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import spray.http._
import spray.json._
import spray.httpx.UnsuccessfulResponseException
import spray.client.pipelining._
import java.util.UUID
import com.parallelai.wallet.datamanager.data.RegistrationValidation
import com.parallelai.wallet.datamanager.data.RegistrationResponse
import scala.Some
import spray.http.HttpResponse
import com.parallelai.wallet.datamanager.data.RegistrationValidationResponse
import com.parallelai.wallet.datamanager.data.UserProfile
import com.parallelai.wallet.datamanager.data.RegistrationRequest


trait DataManagerApiClient extends SprayJsonSupport {
  def getUserProfile(accountId: UUID) : Future[Option[UserProfile]]
  def findUserByMsisdnOrEmail(msisdn: Option[String], email: Option[String]) : Future[Option[UserProfile]]
  
  def findUserForApplication(applicationId: UUID): Future[Option[UserProfile]]

  def register(request: RegistrationRequest) : Future[RegistrationResponse]
  def addApplication(accountId: UUID, applicationId: ApplicationID) : Future[RegistrationResponse]

  def validateRegistration(validation: RegistrationValidation) : Future[RegistrationValidationResponse]

  def updateUserProfile(userProfile: UserProfile): Future[Unit]

}


class DataManagerRestClient(implicit val bindingModule: BindingModule) extends DataManagerApiClient with Injectable {
  implicit val actorSystem = ActorSystem("program-info-client")

  val apiBaseUri = injectOptionalProperty[String]("data.manager.api.url") getOrElse "http://localhost:8080"

  import actorSystem.dispatcher

  val registerPipeline = sendReceive ~> unmarshal[RegistrationResponse]
  val validatePipeline = sendReceive ~> unmarshal[RegistrationValidationResponse]
  val retrieveUserProfilePipeline = sendReceive ~> notFoundToNone ~> unmarshal[Option[UserProfile]]
  val updateProfilePipeline = sendReceive ~> unitIfSuccess
  val validatePwdPipeline = sendReceive

  override def register(request: RegistrationRequest): Future[RegistrationResponse] = {
    val url=s"$apiBaseUri/account"
    println(s"DataManagerRestClient.register: Calling  POST $url $request")
    registerPipeline {
      Post(url, request)
    }
  }

  override def addApplication(accountId: UUID, applicationId: ApplicationID) : Future[RegistrationResponse] = {
    val url=s"$apiBaseUri/account/$accountId/application/$applicationId"
    println(s"DataManagerRestClient.addApplication: Calling  PUT $url")
    registerPipeline { Put( url ) }
  }

  override def validateRegistration(validation: RegistrationValidation): Future[RegistrationValidationResponse] = {
    val url=s"$apiBaseUri/account/application/validation"
    println(s"DataManagerRestClient.validateRegistration: Calling  POST $url $validation")
    validatePipeline { Post( url, validation) }
  }

  override def getUserProfile(accountId: UUID) : Future[Option[UserProfile]] = {
    val url=s"$apiBaseUri/account/$accountId"
    println(s"DataManagerRestClient.getUserProfile: Calling  GET $url")
    retrieveUserProfilePipeline {
      Get(url)
    }
  }


  override def findUserByMsisdnOrEmail(msisdnOp: Option[String], emailOp: Option[String]): Future[Option[UserProfile]] = {
    val findBy = msisdnOp map { msisdn => s"msisdn=$msisdn" } orElse ( emailOp map { email => s"email=$email" } )

    findBy match {
      case Some(query) => {
        val url=s"$apiBaseUri/account?$query"
        println(s"DataManagernRestClient.findUserByMsisdnOrEmail: Calling  GET $url")
        retrieveUserProfilePipeline {
          Get(url)
        }
      }
      case None => Future.failed(new IllegalArgumentException("Invalid identification, need to provide at least one of msisdn and email"))
    }
  }

  def findUserForApplication(applicationId: UUID): Future[Option[UserProfile]] = {
    val url=s"$apiBaseUri/account?applicationId=$applicationId"
    println(s"DataManagerRestClient.findUserForApplication: Calling  GET $url")
    retrieveUserProfilePipeline { Get(url) }
  }

  def updateUserProfile(userProfile: UserProfile): Future[Unit] = {
    val url=s"$apiBaseUri/account/${userProfile.info.userId}"
    println(s"DataManagerRestClient.updateUserProfile: Calling  PUT $url $userProfile")
    updateProfilePipeline { Put(url, userProfile) }
  }

  def unitIfSuccess(response: HttpResponse): Unit =
    if(response.status.isSuccess) ()
    else throw new UnsuccessfulResponseException(response)

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
