package karedo.api.account.impl

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import karedo.api.account.AccountService
import karedo.api.account.entity.MongoDAO
import karedo.api.account.model._
import karedo.common.misc.Util.newUUID

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AccountServiceImpl extends AccountService {
  val collectionName = "UserApp"
  val userAppDAO = new MongoDAO[UserApp] { override def name = "UserApp" }
  val userMobileDAO = new MongoDAO[UserMobile] { override def name = "UserMobile" }
  val userEmailDAO = new MongoDAO[UserEmail] { override def name = "UserEmail" }
  val userAccountDAO = new MongoDAO[UserAccount] { override def name = "UserAccount" }
  val userProfileDAO = new MongoDAO[UserProfile] { override def name = "UserProfile" }

  override def register() = ServiceCall { request =>
    for {
      Some(userApp) <- userAppDAO.findOneById(request.application_id)
      Some(userMobile) <- userMobileDAO.findOneById(request.msisdn)
      if (userApp.account_id == userMobile.account_id)
      Some(userEmail) <- userEmailDAO.findOneById(request.email)
      if (userEmail.account_id != userApp.account_id)
      Some(userAccount) <- userAccountDAO.findOneById(userEmail.account_id)
      Some(userProfile) <- userProfileDAO.findOneById(userAccount._id)
      n <- userProfileDAO.update(userProfile.copy(first_name = Some(request.first_name), last_name = Some(request.last_name)))
      if n > 0
    } yield Done

//
//    val out = userAppDAO.findOneById(
//      request.application_id).map(_.fold(_ => userAppDAO.insert(UserAppChangedEvent(UserApp(id = request.application_id,
//      account_id = newUUID))))
//
//    out
  }
}
