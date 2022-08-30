package karedo.actors

import com.apple.eawt.AppEvent.UserSessionEvent
import karedo.entity.{UserMobile, UserSession}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar138Actor_Login
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar138Actor_Login])

  def exec
  (
    account_id: String,
    application_id: String,
    request:Kar138Req): Result[Error, APIResponse] = {

    logger.info(s"Login\nAccountID: ${account_id}\nApplicationID: ${application_id}")

    val password = request.password

    dbUserApp.find(application_id) match {
      case OK(userApp) => {
        dbUserAccount.find(account_id) match {
          case OK(userAccount) => {
            if( userApp.account_id == userAccount.id ) {
              userAccount.password match {
                case Some(userPassword) => {
                  if( userPassword == password) {
                    val session_id = getNewRandomID
                    val userSession = UserSession(id = session_id, account_id = account_id )
                    dbUserSession.insertNew(userSession) match {
                      case OK(_) => OK(APIResponse(Kar138Res(session_id).toJson.toString , HTTP_OK))
                      case KO(error) => MAKE_ERROR(error, "Unable to create a UserSession object")
                    }
                  } else {
                    OK(APIResponse(ErrorRes(HTTP_UNAUTHORISED, Some("Unauthorised"), "Unauthorised").toJson.toString , HTTP_UNAUTHORISED))
                  }
                }
                case None => MAKE_ERROR("Password not registered. Could be a temporary account.")
              }
            } else {
              OK(APIResponse(ErrorRes(HTTP_CONFLICT, Some("Conflict"), "Account doesn't match records").toJson.toString , HTTP_CONFLICT))
            }
          }
          case KO(error) => {
            logger.error(error)
            OK(APIResponse(ErrorRes(HTTP_NOT_FOUND, Some("Not Found"), "Account not found").toJson.toString , HTTP_NOT_FOUND))
          }
        }
      }
      case KO(error) => {
        logger.error(error)
        OK(APIResponse(ErrorRes(HTTP_NOT_FOUND, Some("Not Found"), "Application not found").toJson.toString , HTTP_NOT_FOUND))
      }
    }
  }
}