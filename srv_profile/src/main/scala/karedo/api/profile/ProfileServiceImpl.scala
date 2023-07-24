package karedo.api.account

import akka.Done
import com.lightbend.lagom.scaladsl.api.ServiceCall
import karedo.api.account.model._
import karedo.common.mongo.reactive.MongoDAO

import scala.concurrent.ExecutionContext.Implicits.global

class ProfileServiceImpl extends ProfileService {
  implicit val prefix = "TEST_"
  val userProfileDAO = new MongoDAO[UserProfile] { override def collectionName = "UserProfile" }

  override def register() = ???
//    ServiceCall { request =>
//    Done
//  }
}
