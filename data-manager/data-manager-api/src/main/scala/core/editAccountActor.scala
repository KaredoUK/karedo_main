package core

import java.util.UUID
import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import core.common.RequestValidationChaining
import org.joda.time.format.ISODateTimeFormat
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.entity.{Brand, UserPersonalInfo, AccountSettings, UserAccount}
import spray.http.parser.HttpParser
import spray.json._
import scala.async.Async._
import scala.concurrent.Future._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import EditAccountActor._
import parallelai.wallet.persistence.LogDAO
import parallelai.wallet.entity.KaredoLog

object EditAccountActor extends ISODateConversion {
  def props(
      userAccountDAO: UserAccountDAO, 
      clientApplicationDAO: ClientApplicationDAO, 
      brandDAO: BrandDAO,
      logDAO: LogDAO): Props =
    Props(new EditAccountActor(userAccountDAO, clientApplicationDAO, brandDAO, logDAO))

  case class GetAccount(accountId: UserID)

  case class GetAccountPoints(accountId: UserID)

  case class FindAccount(deviceId: Option[UserID], msisdn: Option[String], email: Option[String]) extends WithUserContacts

  case class UpdateAccount(userAccount: UserProfile)

  case class DeleteAccount(accountId: UserID)

  case class AddBrand(accountId: UserID, brandId: UUID)

  case class RemoveBrand(user: UserID, brand: UUID )

  case class ListBrandsRequest(brandId: UUID)

  case class GetActiveAccountBrandOffers(accountId:  UserID, brandId: UUID)

  case class GetAcceptedOffers(accountId: UserID)


  sealed trait EditAccountError

  case class UserNotExistent(user: UserID) extends EditAccountError

  case class BrandNotExistent(brand: UUID) extends EditAccountError

  case class BrandAlreadySubscribed(brand: UUID) extends EditAccountError

  case class InternalEditAccountError(throwable: Throwable) extends EditAccountError

  implicit object editAccountErrorJsonFormat extends RootJsonWriter[EditAccountError] {
    def write(error: EditAccountError) = error match {
      case UserNotExistent(user) => JsObject(
        "type" -> JsString("UserNotExistent"),
        "data" -> JsObject {
          "accountID" -> JsString(user.toString)
        }
      )
      case BrandNotExistent(brand) => JsObject(
        "type" -> JsString("BrandNotExistent"),
        "data" -> JsObject {
          "brandID" -> JsString(brand.toString)
        }
      )
      case BrandAlreadySubscribed(brand) => JsObject(
        "type" -> JsString("BrandAlreadySubscribed"),
        "data" -> JsObject {
          "brandID" -> JsString(brand.toString)
        }
      )
      case InternalEditAccountError(reason) => JsObject(
        "type" -> JsString("InternalEditAccountError"),
        "data" -> JsObject {
          "reason" -> JsString(reason.toString)
        }
      )
    }
  }

  def userAccountToUserProfile(userAccount: UserAccount): UserProfile =
    UserProfile(
      UserInfo(
        userId = userAccount.id,
        fullName = userAccount.personalInfo.name,
        postCode = userAccount.personalInfo.postCode,
        birthDate = userAccount.personalInfo.birthDate,
        userType = userAccount.userType,
        country = None,
        email = userAccount.email,
        msisdn = userAccount.msisdn,
        gender = userAccount.personalInfo.gender
      ),
      UserSettings(
        userAccount.settings.maxMessagesPerWeek
      ),
      totalPoints = userAccount.totalPoints
    )

  def userProfileToUserAccount(userProfile: UserProfile): UserAccount =
    UserAccount(
      userProfile.info.userId,
      userProfile.info.msisdn,
      userProfile.info.email,
      userProfile.info.userType,
      UserPersonalInfo(
        userProfile.info.fullName,
        userProfile.info.postCode,
        userProfile.info.birthDate,
        userProfile.info.gender
      ),
      AccountSettings(userProfile.settings.maxAdsPerWeek),
      totalPoints = userProfile.totalPoints
    )
}

class EditAccountActor(
    userAccountDAO: UserAccountDAO, 
    clientApplicationDAO: ClientApplicationDAO, 
    brandDAO: BrandDAO,
    logDAO: LogDAO)
  extends Actor with ActorLogging with RequestValidationChaining with ISODateConversion {

  import context.dispatcher

  def replyToSender[T](response: => ResponseWithFailure[EditAccountError, T]): Unit = {

    Try {
      response
    } recover {
      case t =>
        log.warning("Internal error: {}", t)
        FailureResponse(InternalEditAccountError(t))
    } foreach {
      responseContent: ResponseWithFailure[EditAccountError, T] =>
        log.debug("Returning {}", responseContent)
        sender ! responseContent
    }
  }


  override def receive: Receive = {
    case GetAccount(accountId) =>
      log.info("Trying to get account with account ID {}, sender is {}", accountId, sender)
      replyToSender {
        SuccessResponse(
          userAccountDAO.getById(accountId) map {
            userAccountToUserProfile
          }
        )
      }

    case GetAccountPoints(accountId) =>
      log.info("Trying to get account with account ID {}, sender is {}", accountId, sender)
      replyToSender {
        SuccessResponse(
          userAccountDAO.getById(accountId) map {
            accountInfo => UserPoints(accountInfo.id, accountInfo.totalPoints)
          }
        )
      }

    case FindAccount(applicationIdOp, msisdnOp, emailOp) =>
      log.info("Trying to find account for appId {}, msisdn {} or email {} sender is {}", applicationIdOp, msisdnOp, emailOp, sender)
      replyToSender {
        SuccessResponse(
          userAccountDAO.findByAnyOf(applicationIdOp, msisdnOp, emailOp) map {
            userAccountToUserProfile
          }
        )
      }

    case UpdateAccount(userProfile) =>
      log.info("Trying to update account with id {}", userProfile.info.userId)
      userAccountDAO.update(userProfileToUserAccount(userProfile))
      replyToSender {
        SuccessResponse(StatusResponse("OK"))
      }

    case DeleteAccount(accountId) =>
      log.info("Trying to delete account for userId {} sender is {}", accountId, sender)

      replyToSender(
        SuccessResponse {
          userAccountDAO.delete(accountId)
          StatusResponse("OK")
        }
      )

    case request@AddBrand(accountId, brandId) => replyToSender(addBrand(request))

    case request@RemoveBrand(accountId, brandId) => replyToSender(removeBrand(request))

    case ListBrandsRequest(accountId) => replyToSender(listBrands(accountId))

    case GetActiveAccountBrandOffers(user,brand) => replyToSender(getActiveAccountBrandOffers(user,brand))
  }

  def getActiveAccountBrandOffers(user: UserID, brand: UUID):
    ResponseWithFailure[EditAccountError,GetActiveAccountBrandOffersResponse] =  {
     userAccountDAO.getById(user) match {
      case None => FailureResponse(UserNotExistent(user))
      case Some(_) =>
        SuccessResponse(GetActiveAccountBrandOffersResponse(brandDAO.listActiveAds(brand).size))
    }


  }

  def validAddBrand(addBrand: AddBrand): Option[EditAccountError] = {
    val AddBrand(user, brand) = addBrand

    userAccountDAO.getById(user) match {
      case None => Some(UserNotExistent(user))
      case _ => {
        brandDAO.getById(brand) // brand is not future so no need to await
        match {
          case None => Some(BrandNotExistent(brand))
          case _ => {

            userAccountDAO.getBrand(user, brand) match {
              case Some(s) => Some (BrandAlreadySubscribed (brand) )
              case None => None
            }
          }
        }
      }
    }
  }

  def listBrands(accountId: UserID): ResponseWithFailure[EditAccountError, List[BrandRecord]] = {
    val list = userAccountDAO.listUserSubscribedBrands(accountId).map {
      id =>
        brandDAO getById (id) map {
          brand => BrandRecord(brand.id, brand.name,
            brand.createDate,
            brand.startDate,
            brand.endDate,
            brand.iconId)
        }
    } filter {
      _.isDefined
    } map {
      _.get
    }

    SuccessResponse(list)
  }

  def addBrand(request: AddBrand): ResponseWithFailure[EditAccountError, StatusResponse] =

    withValidations(request)(validAddBrand) {
      request => {

        val AddBrand(user, brand) = request
        log.info("Trying to add brand {}, to user {}, sender is ", brand, user, sender)

        userAccountDAO.addBrand(user, brand)
        logDAO.addLog(KaredoLog(user=Some(user), brand=Some(brand), logType=Some("ADDBRAND")))
        

        SuccessResponse(StatusResponse("OK"))
      }
    }

  def removeBrand(request: RemoveBrand): ResponseWithFailure[EditAccountError, StatusResponse] = {


    val RemoveBrand(user, brand) = request
    log.info("Trying to remove brand {}, from user {}, sender is ", brand, user, sender)

    userAccountDAO.deleteBrand(user, brand)
    logDAO.addLog(KaredoLog(user=Some(user), brand=Some(brand), logType=Some("REMOVEBRAND")))

    SuccessResponse(StatusResponse("OK"))

  }
}
