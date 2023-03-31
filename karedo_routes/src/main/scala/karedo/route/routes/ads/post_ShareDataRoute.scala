package karedo.route.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp, UserInteraction}
import karedo.route.routes.KaredoRoute
import karedo.route.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

/**
  * Created by pakkio on 10/3/16.
  */


object post_ShareDataRoute extends KaredoRoute
  with post_ShareDataActor {

  def route = {
    Route {
      path("account" / Segment / "ad" / "share_data") {
        accountId =>
              optionalHeaderValueByName("X_Identification") {
                deviceId =>
                  post {
                    entity(as[post_ShareDataRequest]) {
                      request =>
                        doCall({
                          exec(accountId, deviceId, request)
                        }
                        )
                    }
                  }
              }

      }
    }
  }
}

trait post_ShareDataActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoUtils {
  override val logger = LoggerFactory.getLogger(classOf[post_ShareDataActor])

  def exec(accountId: String, deviceId: Option[String], request: post_ShareDataRequest): Result[Error, APIResponse] = {
    val applicationId = request.application_id
    val sessionId = request.session_id
    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\nsessionId: $sessionId")
    authenticate(accountId, deviceId, applicationId, Some(sessionId), allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {

        Try[Result[Error, APIResponse]] {

          val account_hash = storeAccountHash(accountId) match {
            case OK(h) => h
            case KO(h) => h
          }

          val reqChannels = request.share.channels.getOrElse(List())

          val outChannels = reqChannels.map(c => {
            c.channel match {
              case SOCIAL_EMAIL => {
                val userProfile = dbUserProfile.find(accountId).get
                val from_name = userProfile.first_name.getOrElse("Someone") + " " + userProfile.last_name.getOrElse("")

                val email_imp_url_code = storeUrlMagic(request.share.imp_url, None) match {
                  case OK(u) => u
                  case KO(u) => u
                }
                val email_imp_url = s"${url_magic_share_base}/shr?u=${email_imp_url_code}&v=${account_hash}"

                val email_click_url_code = storeUrlMagic(request.share.click_url, None) match {
                  case OK(u) => u
                  case KO(u) => u
                }
                val email_click_url = s"${url_magic_share_base}/shr?u=${email_click_url_code}&v=${account_hash}"

                val email_address = if( c.channel_id != null )
                  c.channel_id
                else
                  dbUserAccount.findActiveEmail(accountId) match {
                    case OK(email) => email.address
                    case KO(_) => throw MAKE_THROWABLE("No valid email address found")
                  }

                val email_share_data = share.html.email_share.render( from_name,
                  request.share.ad_text.getOrElse("Shared from Karedo"), email_imp_url, email_click_url ).toString

                sendEmail(email_address, "Shared from Karedo", email_share_data)

                c.copy(share_data = Some(email_share_data), share_url = Some(email_click_url))
              }
              case _ => {
                val url_code = storeUrlMagic(request.share.imp_url, Some(request.share.click_url)) match {
                  case OK(u) => u
                  case KO(u) => u
                }

                val share_url = s"${url_magic_share_base}/shr?u=${url_code}&v=${account_hash}"

                val social_share_data = request.share.ad_text.getOrElse("Shared from Karedo")

                c.copy(share_data = Some(social_share_data), share_url = Some(share_url))
              }
            }
          })

          dbUserShare.insertNew(UserInteraction(account_id = accountId, interaction = request.share.copy(channels = Some(outChannels))))

          OK(APIResponse(SocialChannelListResponse(outChannels).toJson.toString, code))
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
      }
    )
  }
}





