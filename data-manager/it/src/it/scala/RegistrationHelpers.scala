import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import restapi.security.AuthenticationSupport._
import spray.client.pipelining._
import spray.http.HttpRequest

import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import parallelai.wallet.util.SprayJsonSupport._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

case class Registration(application:ApplicationID, pass:String, userId:UUID, sessionId: String)
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
    Registration(applicationId, pass, userId, sessionId)
  }

  def ResetAccount = {
    val r = RegisterAccount

    val activationCode0=ca.getById(r.application).get.activationCode

    val applicationId: ApplicationID = UUID.randomUUID()

    try {
      doResetApplication(r.application, r.userId)
      "should fail if called with same applicationId" == ""
    } catch { case e:Throwable => println("Correctly received exception "+e.getMessage) }
    doResetApplication(applicationId,r.userId)


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
trait BrandHelpers {
  this: MyUtility =>

  def addBrand(sessionId:String, name:String): UUID = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[BrandResponse]

    val brandR = wait {
      add {
        Post( s"$serviceUrl/brand",BrandData(name=name,iconId="11111"))
      }
    }
    brandR.id
  }
  def addAd(sessionId:String, brand: UUID, ad:String): UUID = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[AdvertDetailResponse]

    val adR = wait {
      add {
        Post( s"$serviceUrl/brand/$brand/advert",AdvertDetail(text=ad,imageIds = List(), value=10 ))

      }
    }
    adR.id
  }

  def listAds(sessionId:String, brand: UUID): List[AdvertDetailResponse] = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[List[AdvertDetailResponse]]

    val adR = wait {
      add {
        Get( s"$serviceUrl/brand/$brand/advert")

      }
    }
    adR
  }
}
