package karedo.actors.ads

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by pakkio on 10/8/16.
  */


trait Kar166_interaction_actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar166_interaction_actor])

  def exec(accountId: String, deviceId: Option[String], request: Kar166Request): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = request.session_id
    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, Some(sessionId), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          Future {
            request.entries.foreach(x => {
              val iu = UserInteraction(account_id = accountId, interaction = x)

              dbUserInteract.insertNew(iu)
            })
          }

          OK(APIResponse("", code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}

trait Kar167_share_data_actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar167_share_data_actor])

  def exec(accountId: String, deviceId: Option[String], request: Kar167Request): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = request.session_id
    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, Some(sessionId), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {
          val url_code = storeUrlMagic(request.share.imp_url, Some(request.share.click_url)) match {
            case OK(u) => u
            case KO(u) => u
          }

          val account_hash = storeAccountHash(accountId) match {
            case OK(h) => h
            case KO(h) => h
          }

          val share_url = s"${url_magic_share_base}/shr?u=${url_code}&v=${account_hash}"

          val social_share_data = "Karedo Social"
          val email_share_date = "Karedo Email"

          val outChannels = request.share.channels.getOrElse(List()).map(c => {
            val share_data = c.channel match {
              case SOCIAL_EMAIL => email_share_date
              case _ => social_share_data
            }

            c.copy(share_data = Some(share_data), share_url = Some(share_url))
          })

          dbUserShare.insertNew(UserInteraction(account_id = accountId, interaction = request.share.copy(channels = Some(outChannels))))

          OK(APIResponse(Kar167Res(outChannels).toJson.toString, code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}

trait Kar165_postFavourite_actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar165_postFavourite_actor])

  def exec(accountId: String, deviceId: Option[String], request: Kar165Request): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = request.session_id
    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, Some(sessionId), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          dbUserFavourite.find(accountId) match {
            case OK(userFav) => {
              val newFavs = request.favourites.map(f => f.copy(ts = None, favourite = None))
              val oldFavs = userFav.entries.filter(f => ! newFavs.contains(f.copy(ts = None, favourite = None)))
              val allfavs = request.favourites ++ oldFavs

              dbUserFavourite.update(userFav.copy(entries = allfavs))
            }
            case KO(_) => {
              dbUserFavourite.insertNew(UserFavourite(accountId, request.favourites))
            }
          }

          OK(APIResponse("", code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}

trait Kar165_getFavourite_actor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers {
  override val logger = LoggerFactory.getLogger(classOf[Kar165_getFavourite_actor])
//accountId, deviceId, applicationId, sessionId
  def exec(accountId: String, deviceId: Option[String], applicationId:String, sessionId: Option[String]): Result[Error, APIResponse] = {
    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          dbUserFavourite.find(accountId) match {
            case OK(userFav) => {
              OK(APIResponse(Kar165Res(userFav.entries).toJson.toString, code))
            }
            case KO(_) => {
              OK(APIResponse("", code))
            }
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}

