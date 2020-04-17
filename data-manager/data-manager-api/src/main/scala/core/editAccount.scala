  case class FindAccount(msisdn: Option[String], email: Option[String]) extends WithUserContacts
}

import EditAccountActor._

class EditAccountActor(userAccountDAO : UserAccountDAO, clientApplicationDAO : ClientApplicationDAO) extends Actor with ActorLogging {
  import context.dispatcher

  override def receive: Receive = {
    case GetAccount(accountId) =>
      log.info("Trying to get account with account ID {}", accountId)
      userAccountDAO.getById(accountId) map { _ map { userAccountToUserProfile } }

    case FindAccount(msisdnOp, emailOp) =>
      log.info("Trying to find account with msisdn {} or email {}", msisdnOp, emailOp)
      userAccountDAO.findByAnyOf(None, msisdnOp, emailOp) map { _ map { userAccountToUserProfile } }
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
