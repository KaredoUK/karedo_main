package karedo.api.account

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import karedo.api.account.model._
import karedo.common.mongo.reactive.MongoDAO

import scala.concurrent.ExecutionContext.Implicits.global

class AccountServiceImpl extends AccountService {
  implicit val prefix = "TEST_"
  val userAppDAO = new MongoDAO[UserApp] { override def collectionName = "UserApp" }
  val userMobileDAO = new MongoDAO[UserMobile] { override def collectionName = "UserMobile" }
  val userEmailDAO = new MongoDAO[UserEmail] { override def collectionName = "UserEmail" }
  val userAccountDAO = new MongoDAO[UserAccount] { override def collectionName = "UserAccount" }
  val userProfileDAO = new MongoDAO[UserProfile] { override def collectionName = "UserProfile" }

  override def register() = ServiceCall { request =>
    val userAppF = userAppDAO.findOneById(request.application_id)
    val userMobileF = userMobileDAO.findOneById(request.msisdn)
    val userEmailF = userEmailDAO.findOneById(request.email)

    // App:Known  Mobile:Known  Email:Known
    for {
      Some(userApp) <- userAppF
      Some(userMobile) <- userMobileF
      Some(userEmail) <- userEmailF
      if userApp.account_id == userMobile.account_id
      if userApp.account_id == userEmail.account_id
    } yield userApp


//    val o = userAppDAO.findOneById(request.application_id).flatMap {
//      case Some(userApp) => userMobileDAO.findOneById(request.msisdn).flatMap {
//        case Some(userMobile) => userEmailDAO.findOneById(request.email).flatMap {
//          case Some(userEmail) =>
//          case None =>
//        }
//        case None =>
//      }
//      case None => Future.successful(Done)
//    }


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
  }
}
