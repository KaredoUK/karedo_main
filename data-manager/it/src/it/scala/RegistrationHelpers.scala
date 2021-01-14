import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import spray.client.pipelining._
import spray.http.HttpRequest

import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import parallelai.wallet.util.SprayJsonSupport._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pakkio on 13/02/2015.
 */
trait RegistrationHelpers {
  this: MyUtility =>

  def RegisterAccount = {
    val applicationId: ApplicationID = UUID.randomUUID()
    val msisdn = generateMobile

    doRegister(applicationId, msisdn)

    // get activation code of this one
    val activationCode = ca.getById(applicationId).get.activationCode

    val pass: String = "PASS"
    val userId = doValidate(applicationId, activationCode, pass)


    val sessionId = doLogin(applicationId, userId, pass)
    (applicationId, pass, userId, sessionId)
  }

  def ResetAccount = {
    val (applicationId0, pass0, userId, sessionId) = RegisterAccount

    val activationCode0=ca.getById(applicationId0).get.activationCode

    val applicationId: ApplicationID = UUID.randomUUID()

    try {
      doResetApplication(applicationId0, userId)
      "should fail if called with same applicationId" == ""
    } catch { case e:Throwable => println("Correctly received exception "+e.getMessage) }
    doResetApplication(applicationId,userId)


    // get activation code of this one
    val activationCode = ca.getById(applicationId).get.activationCode



    val pass: String = "PASS"
    doValidate(applicationId, activationCode, pass)

    (applicationId, activationCode0, activationCode)
  }


  //val pipeline = sendReceive ~> unmarshal[RegistrationResponse]
  def doRegister(applicationId: ApplicationID, msisdn: String): Unit = {

    // execution context for futures
    val register: HttpRequest => Future[AddApplicationResponse] =
      sendReceive ~> unmarshal[AddApplicationResponse]

    val registrationResponse = wait {
      register {
        Post(s"$serviceUrl/account", AddApplicationRequest(applicationId, Some(msisdn), None))
      }
    }
  }

  def doValidate(applicationId: ApplicationID, activationCode: String, password: String): UUID = {

    // execution context for futures
    val validate: HttpRequest => Future[RegistrationValidationResponse] =
      sendReceive ~> unmarshal[RegistrationValidationResponse]

    val validationResponse = wait {
      validate {
        Post(s"$serviceUrl/account/application/validation", RegistrationValidation(applicationId, activationCode, Some(password)))
      }
    }
    validationResponse.userID
  }

  def doLogin(applicationId: ApplicationID, userId: UUID, password: String): String = {

    // execution context for futures
    val login = sendReceive ~> unmarshal[APISessionResponse]

    val loginResponse = wait {
      login {
        Post(s"$serviceUrl/account/${userId}/application/${applicationId}/login", APILoginRequest(password = password))
      }
    }

    loginResponse.sessionId
  }

  def doResetApplication(applicationId: ApplicationID, userId:UUID): Unit = {
    val reset = sendReceive ~> unmarshal[RegistrationResponse]

    val resetResponse = wait {
      reset {
        Put( s"$serviceUrl/account/${userId}/application/${applicationId}/reset")
      }
    }


  }
}
