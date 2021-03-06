import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import core.ISODateConversion
import parallelai.wallet.entity.KaredoTypes.KaredoPoints
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import restapi.security.AuthenticationSupport._
import spray.client.pipelining._
import spray.http._

import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
import parallelai.wallet.util.SprayJsonSupport._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

case class Registration(application: ApplicationID, pass: String, userId: UUID, sessionId: String)

/**
 * Created by pakkio on 13/02/2015.
 */
trait RegistrationHelpers {
  this: ItEnvironment =>

  def RegisterAccount = {
    val applicationId: ApplicationID = UUID.randomUUID()
    val msisdn = generateMobile
    val email = generateEmail

    doRegister(applicationId, msisdn)

    // get activation code of this one
    val activationCode = ca.getById(applicationId).get.activationCode

    val pass: String = "PASS"
    val userId = doValidate(applicationId, activationCode, pass)


    val sessionId = doLogin(applicationId, userId, pass)
    Registration(applicationId, pass, userId, sessionId)
  }

  def clearAll() = {
    ca.dao.collection.getDB.dropDatabase()
  }

  def ResetAccount = {
    val r = RegisterAccount

    val activationCode0 = ca.getById(r.application).get.activationCode

    val applicationId: ApplicationID = UUID.randomUUID()

    try {
      doResetApplication(r.application, r.userId)
      "should fail if called with same applicationId" == ""
    } catch {
      case e: Throwable => println("Correctly received exception " + e.getMessage)
    }
    doResetApplication(applicationId, r.userId)


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
        Post(s"$serviceUrl/account", AddApplicationRequest(applicationId, Some(msisdn), "CUSTOMER", None))
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

  def doResetApplication(applicationId: ApplicationID, userId: UUID): Unit = {
    val reset = sendReceive ~> unmarshal[RegistrationResponse]

    val resetResponse = wait {
      reset {
        Put(s"$serviceUrl/account/${userId}/application/${applicationId}/reset")
      }
    }


  }
}

trait BrandHelpers extends ISODateConversion {
  this: ItEnvironment =>

  def addBrand(sessionId: String, name: String): UUID = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[BrandResponse]

    val brandR = wait {
      add {
        Post(s"$serviceUrl/brand", BrandData(name = name, iconId = "11111"))
      }
    }
    brandR.id
  }

  def addBrandInteraction(sessionId: String, i:UserBrandInteraction): KaredoPoints = {
    val addInteraction = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[InteractionResponse]

    val ir = wait {
      addInteraction {
        Post(s"$serviceUrl/user/${i.userId}/interaction/brand",
          UserBrandInteraction(userId = i.userId, brandId = i.brandId, interaction = i.interaction, intType = i.intType ))
      }
    }
    ir.userTotalPoints
  }

  def addOfferInteraction(sessionId: String, i: UserOfferInteraction): KaredoPoints = {
    val addInteraction = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[InteractionResponse]

    val ir = wait {
      addInteraction {
        Post(s"$serviceUrl/user/${i.userId}/interaction/offer",
          UserOfferInteraction(userId = i.userId, offerId = i.offerId, interaction = i.interaction, intType = i.intType))
      }
    }
    ir.userTotalPoints
  }

  def getOfferCode(r: Registration, ad: UUID): String ={
    val getOffer = addHeader(HEADER_NAME_SESSION_ID, r.sessionId) ~> sendReceive ~> unmarshal[GetOfferCodeResponse]
    case class MyRequest(userId:UUID,adId:UUID)
    implicit val v=jsonFormat2(MyRequest)
    val offerR = wait {
      getOffer {
        Post(s"$serviceUrl/user/${r.userId}/getcode",MyRequest(r.userId,ad))
      }
    }
    offerR.code
  }

  def addAd(sessionId: String, brand: UUID, ad: String): UUID = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[AdvertDetailListResponse]

    val adR = wait {
      add {
        Post(s"$serviceUrl/brand/$brand/advert", AdvertDetailApi(
          shortText = ad,
          detailedText = "detailed",
          termsAndConditions = "T&C",
          startDate=ISONowPlus(0),
          endDate=ISONowPlus(20),
          imageIds = List(),
          summaryImages = List(),
          karedos = 10))

      }
    }
    adR.offerId
  }

  def getAd(sessionId: String, brand: UUID, ad: String): AdvertSummaryResponse = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[AdvertSummaryResponse]

    val adR = wait {
      add {
        Get(s"$serviceUrl/brand/$brand/advert/$ad")

      }
    }
    adR
  }

  def listAds(sessionId: String, brand: UUID): List[AdvertSummaryResponse] = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[List[AdvertSummaryResponse]]

    val adR = wait {
      add {
        Get(s"$serviceUrl/brand/$brand/summary")

      }
    }
    adR
  }

  def addBrandToUser(sessionId: String, user: UUID, brand: UUID): String = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[String]

    val result = wait {
      add {
        Post(s"$serviceUrl/account/$user/brand", BrandIDRequest(brand))
      }
    }
    result
  }

  def removeBrandFromUser(sessionId: String, user: UUID, brand: UUID): String = {
    val add = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[String]

    val result = wait {
      add {
        Delete(s"$serviceUrl/account/$user/brand/$brand")
      }
    }
    result
  }


  def listBrandsForUser(sessionId: String, user: UUID): List[BrandRecord] = {
    val list = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[List[BrandRecord]]

    val ret = wait {
      list {
        Get(s"$serviceUrl/account/$user/brand")
      }
    }
    ret
  }

  def getSuggestedAds(sessionId: String, user: UUID, brand: UUID, max: Int): List[AdvertDetailResponse] = {
    val list = addHeader(HEADER_NAME_SESSION_ID, sessionId) ~> sendReceive ~> unmarshal[List[AdvertDetailResponse]]

    val ret = wait {
      list {
        Get(s"$serviceUrl/account/$user/brand/$brand/ads?max=$max")
      }
    }
    ret
  }

  def addMedia(sessionId: String, name: String, contentType: String, bytes: Array[Byte]): AddMediaResponse = {
    val add =
      addHeader("Content-Type", "multipart/form-data") ~>
        addHeader("X-Content-Type", contentType) ~>
        addHeader(HEADER_NAME_SESSION_ID, sessionId) ~>
        sendReceive ~>
        unmarshal[AddMediaResponse]

    val parts = contentType.split("/")
    val (p1,p2) = (parts(0),parts(1))
    val mediaType=MediaTypes.getForKey((p1,p2)).get
    val ret = wait {
      add {
        Post(s"$serviceUrl/media",
          MultipartFormData(Map(
            name -> BodyPart(
              HttpEntity(mediaType, bytes))
            )
          ))
      }
    }
    ret
  }

  def getMedia(sessionId: String, imageId: String): Array[Byte] = {
    val get =
      addHeader(HEADER_NAME_SESSION_ID, sessionId) ~>
        sendReceive

    val ret = wait {
      get {
        Get(s"$serviceUrl/media/$imageId")
      }
    }
    ret.entity.data.toByteArray
  }
}
