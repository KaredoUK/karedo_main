package core

import java.util.UUID

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import parallelai.wallet.persistence.{BrandDAO, ClientApplicationDAO, UserAccountDAO}
import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.entity.{UserPersonalInfo, AccountSettings, UserAccount}
import spray.json.{JsValue, JsString, JsObject, RootJsonFormat}
import scala.async.Async._
import scala.concurrent.Future._
import scala.concurrent.{ExecutionContext, Future}

object EditAccountActor {
  def props(userAccountDAO: UserAccountDAO, clientApplicationDAO: ClientApplicationDAO, brandDAO: BrandDAO): Props =
    Props(classOf[EditAccountActor], userAccountDAO, clientApplicationDAO, brandDAO)

  case class GetAccount(accountId: UserID)

  case class GetAccountPoints(accountId: UserID)

  case class FindAccount(applicationId: Option[UserID], msisdn: Option[String], email: Option[String]) extends WithUserContacts

  case class UpdateAccount(userAccount: UserProfile)

  case class AddBrand(accountId: UserID, brandId: UUID)

  def withValidations[Request, Error, Response](request: Request)(validations: (Request => Future[Option[Error]])*)
                                               (successFlow: Request => Future[Either[Error, Response]])
                                               (implicit executionContext: ExecutionContext)
  : Future[Either[Error, Response]] = {
    val validationResult = validations.foldLeft[Future[Option[Error]]](successful(None)) {
      case (currStatus, currFunction) =>
        currStatus flatMap {
          _ match {
            case Some(_) => currStatus
            case None => currFunction(request)
          }
        }
    }
    validationResult flatMap {
      _ match {
        case Some(validationError) => successful(Left(validationError))
        case None => successFlow(request)
      }
    }
  }

  sealed trait EditAccountError

  case class UserNotExistent(user: UserID) extends EditAccountError

  case class BrandNotExistent(brand: UUID) extends EditAccountError

  case class BrandAlreadySubscribed(brand: UUID) extends EditAccountError

  case class InternalEditAccountError(throwable: Throwable) extends EditAccountError

  case object Empty extends EditAccountError

  implicit object EditAccountErrorJsonFormat extends RootJsonFormat[EditAccountError] {
    def write(error: EditAccountError) = error match {
      case UserNotExistent(user) => JsObject(
        "type" -> JsString("UserNotExistent"),
        "data" -> JsObject {
          "accountID" -> JsString(user.toString)
        }
      )
      case BrandNotExistent(brand)  => JsObject(
        "type" -> JsString("BrandNotExistent"),
        "data" -> JsObject {
          "brandID" -> JsString(brand.toString)
        }
      )
      case BrandAlreadySubscribed(brand)  => JsObject(
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
      case Empty => JsString("Empty")
    }

    override def read(json: JsValue): EditAccountError = Empty
  }

}

import EditAccountActor._

class EditAccountActor(userAccountDAO: UserAccountDAO, clientApplicationDAO: ClientApplicationDAO, brandDAO: BrandDAO) extends Actor with ActorLogging {

  import context.dispatcher

  def replyToSender[T](responseFuture: Future[T]): Unit = {
    val requester = sender
    responseFuture onSuccess { case response =>
      log.info("Completed, replying to {} with response {}", requester.path, response)
      requester ! response
    }
  }

  override def receive: Receive = {
    case GetAccount(accountId) =>
      log.info("Trying to get account with account ID {}, sender is {}", accountId, sender)
      replyToSender {
        userAccountDAO.getById(accountId) map {
          _ map {
            userAccountToUserProfile
          }
        }
      }

    case GetAccountPoints(accountId) =>
      log.info("Trying to get account with account ID {}, sender is {}", accountId, sender)
      replyToSender {
        userAccountDAO.getById(accountId) map {
          _ map { accountInfo => UserPoints(accountInfo.id, accountInfo.totalPoints)}
        }
      }

    case FindAccount(applicationIdOp, msisdnOp, emailOp) =>
      log.info("Trying to find account for appId {}, msisdn {} or email {} sender is {}", applicationIdOp, msisdnOp, emailOp, sender)
      replyToSender {
        userAccountDAO.findByAnyOf(applicationIdOp, msisdnOp, emailOp) map {
          _ map {
            userAccountToUserProfile
          }
        }
      }

    case UpdateAccount(userProfile) =>
      log.info("Trying to update account with id {}", userProfile.info.userId)
      userAccountDAO.update(userProfileToUserAccount(userProfile))

    case request@AddBrand(accountId, brandId) => replyToSender(addBrand(request))


  }

  def validAddBrand(addBrand: AddBrand): Future[Option[EditAccountError]] = async {
    val AddBrand(user, brand) = addBrand
    await {
      userAccountDAO.getById(user)
    } match {
      case None => Some(UserNotExistent(user))
      case _ => {
        brandDAO.getById(brand) // brand is not future so no need to await
        match {
          case None => Some(BrandNotExistent(brand))
          case _ => {

            if (await {
              userAccountDAO.getBrand(user, brand)
            })
              Some(BrandAlreadySubscribed(brand))
            else None
          }
        }
      }
    }
  }

  def addBrand(request: AddBrand): Future[Either[EditAccountError, Unit]] =

    withValidations(request)(validAddBrand) {
      request => {

        val AddBrand(user, brand) = request
        log.info("Trying to add brand {}, to user {}, sender is ", brand, user, sender)

        userAccountDAO.addBrand(user, brand)

        successful(Right(""))
      }
    }


  def userAccountToUserProfile(userAccount: UserAccount): UserProfile =
    UserProfile(
      UserInfo(
        userId = userAccount.id,
        fullName = userAccount.personalInfo.name,
        postCode = userAccount.personalInfo.postCode,
        birthDate = userAccount.personalInfo.birthDate,
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
