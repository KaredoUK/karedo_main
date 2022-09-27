package karedo.actors.login

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.entity.dao.{DbMongoDAO, Keyable}
import karedo.util.Util.now
import karedo.util.{Result, _}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar141_DeleteAccount_actor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
    with DefaultActorSystem
{

  override val logger = LoggerFactory.getLogger(classOf[Kar141_DeleteAccount_actor])

  def exec(
            deviceId: Option[String],
            request: Kar141_DeleteAccount_Req
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