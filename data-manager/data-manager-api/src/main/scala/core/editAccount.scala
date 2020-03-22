package core

import akka.actor.{Props, ActorLogging, Actor}
import akka.actor.Actor.Receive
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import parallelai.wallet.persistence.cassandra.UserAccountCassandraDAO
import com.parallelai.wallet.datamanager.data._
import parallelai.wallet.entity.UserAccount

object EditAccountActor {
  def props(userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO): Props =
    Props(classOf[EditAccountActor], userAccountDAO, clientApplicationDAO)

  case class GetAccount(accountId: UserID)
}

import EditAccountActor._

class EditAccountActor(userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO) extends Actor with ActorLogging {
  import context.dispatcher

  override def receive: Receive = {
    case GetAccount(accountId) => userAccountDAO.getById(accountId) map { _ map { userAccountToUserProfile } }
  }

  def userAccountToUserProfile(userAccount: UserAccount): UserProfile =
    UserProfile(
      UserInfo(
        userId = userAccount.id,
        fullName = userAccount.personalInfo.name,
        postCode = userAccount.personalInfo.postCode,
        address = None,
        country = None,
        email = userAccount.email,
        msisdn = userAccount.msisdn
      ),
      UserSettings(
        userAccount.settings.maxMessagesPerWeek
      )
    )
}
