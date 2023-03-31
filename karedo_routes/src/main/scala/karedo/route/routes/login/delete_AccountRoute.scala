package karedo.route.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.common.akka.DefaultActorSystem
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/3/16.
  */
object delete_AccountRoute extends KaredoRoute
  with delete_AccountActor {

  def route = {
    Route {

      // DELETE /account
      path("account") {
        optionalHeaderValueByName("X_Identification") {
          deviceId =>
            delete {
              entity(as[delete_AccountRequest]) {
                request =>
                  doCall({
                    exec(deviceId, request)
                  }
                  )
              }
            }
        }
      }
    }
  }
}

trait delete_AccountActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
    with DefaultActorSystem
{

  override val logger = LoggerFactory.getLogger(classOf[delete_AccountActor])

  def exec(
            deviceId: Option[String],
            request: delete_AccountRequest
          ): Result[Error, APIResponse] = {
    val accountId = request.account_id
    val applicationId = request.application_id
    val sessionId = Some(request.session_id)

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val userApp = dbUserApp.find(applicationId).get
          val userAccount = dbUserAccount.find(accountId).get
          if(userAccount.id != userApp.account_id) throw MAKE_THROWABLE("Account IDs don't match")

          //          def deleteByAccount(d: DbMongoDAO[String, Keyable[String]])
          //          = {
          //            d.findByAccount(accountId) match {
          //              case OK(l) => l.foreach(x => d.delete(x))
          //            }
          //          }

          dbUserApp.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserApp.delete(_)) case _ =>}
          dbUserMobile.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserMobile.delete(_)) case _ =>}
          dbUserEmail.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserEmail.delete(_)) case _ =>}
          dbUserSession.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserSession.delete(_)) case _ =>}
          dbUserInteract.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserInteract.delete(_)) case _ =>}
          dbUserShare.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserShare.delete(_)) case _ =>}
          dbUserMessages.findByAccount(accountId) match {case OK(l) => l.foreach(dbUserMessages.delete(_)) case _ =>}

          dbUserFavourite.delete(dbUserFavourite.find(accountId).get)
          dbUserIntent.delete(dbUserIntent.find(accountId).get)
          dbUserKaredos.delete(dbUserKaredos.find(accountId).get)
          dbUserPrefs.delete(dbUserPrefs.find(accountId).get)
          dbUserProfile.delete(dbUserProfile.find(accountId).get)

          dbUserAccount.delete(userAccount)

          OK(APIResponse("", code))

        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }

}