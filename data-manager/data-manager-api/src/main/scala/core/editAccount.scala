package core

import java.util.UUID

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.entity.{UserPersonalInfo, AccountSettings, UserAccount}
import scala.concurrent.Future

object EditAccountActor {
  def props(userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO): Props =
    Props(classOf[EditAccountActor], userAccountDAO, clientApplicationDAO)

  case class GetAccount(accountId: UserID)

  case class GetAccountPoints(accountId: UserID)

  case class FindAccount(applicationId: Option[UserID], msisdn: Option[String], email: Option[String]) extends WithUserContacts

  case class UpdateAccount(userAccount: UserProfile)

  case class AddBrand(accountId:UserID, brandId:UUID)

}

import EditAccountActor._

class EditAccountActor(userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO) extends Actor with ActorLogging {
  import context.dispatcher

  def replyToSender[T](responseFuture: Future[T]): Unit = {
    val requester = sender
    responseFuture onSuccess  { case response =>
      log.info("Completed, replying to {} with response {}", requester.path, response)
      requester ! response
    }
  }

  override def receive: Receive = {
    case GetAccount(accountId) =>
      log.info("Trying to get account with account ID {}, sender is {}", accountId, sender)
      replyToSender {
        userAccountDAO.getById(accountId) map { _ map { userAccountToUserProfile } }
      }

    case GetAccountPoints(accountId) =>
      log.info("Trying to get account with account ID {}, sender is {}", accountId, sender)
      replyToSender {
        userAccountDAO.getById(accountId) map { _ map { accountInfo => UserPoints(accountInfo.id, accountInfo.totalPoints) } }
      }

    case FindAccount(applicationIdOp, msisdnOp, emailOp) =>
      log.info("Trying to find account for appId {}, msisdn {} or email {} sender is {}", applicationIdOp, msisdnOp, emailOp, sender)
      replyToSender {
        userAccountDAO.findByAnyOf(applicationIdOp, msisdnOp, emailOp) map { _ map { userAccountToUserProfile } }
      }

    case UpdateAccount(userProfile) =>
      log.info("Trying to update account with id {}", userProfile.info.userId)
      userAccountDAO.update(userProfileToUserAccount(userProfile))

    case AddBrand(accountId, brandId) =>
      userAccountDAO.addBrand(accountId,brandId)
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
