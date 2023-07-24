package karedo.route.routes.login

/**
  * Created by crajah on 14/10/2016.
  */
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.UserSession
import karedo.common.jwt.JWTMechanic
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers, KaredoUtils}
import karedo.route.routes.KaredoRoute
import org.slf4j.LoggerFactory
import karedo.common.result.{KO, OK, Result}
import karedo.route.routes.prefs.get_PrefsRoute.AUTH_HEADER_NAME

/**
  * Created by pakkio on 10/3/16.
  */
object post_LoginRoute extends KaredoRoute
  with post_LoginActor {

  def route = {
    Route {
      // POST /account/{{account_id}}/application/{{application_id}}/login
      path("login") {
        optionalHeaderValueByName(AUTH_HEADER_NAME) {
          deviceId =>
            post {
              entity(as[post_LoginRequest]) {
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

trait post_LoginActor
  extends DbCollections
    with KaredoAuthentication
    with KaredoJsonHelpers
    with KaredoConstants
    with KaredoUtils
    with JWTMechanic
{
  override val logger = LoggerFactory.getLogger(classOf[post_LoginActor])

  def exec( deviceId: Option[String],
            request:post_LoginRequest): Result[Error, APIResponse] = {

    val account_id = request.account_id
    val application_id = request.application_id

    logger.debug(s"Login\nAccountID: ${account_id}\nApplicationID: ${application_id}")

    val password = request.password

    dbUserApp.find(application_id) match {
      case OK(userApp) => {
        dbUserAccount.find(account_id) match {
          case OK(userAccount) => {
            if( userApp.account_id == userAccount.id ) {
              userAccount.password match {
                case Some(userPassword) => {
                  if( doesPasswordMatch(account_id, password, userPassword ) ) { // userPassword == password) {
                    val session_id = getNewRandomID
                    val userSession = UserSession(id = session_id, account_id = account_id )
                    dbUserSession.insertNew(userSession) match {
                      case OK(_) => OK(APIResponse(SessionIdResponse(session_id).toJson.toString , HTTP_OK_200,
                        jwt = getJWT(application_id, account_id, Some(session_id))))
                      case KO(error) => MAKE_ERROR(error, "Unable to create a UserSession object")
                    }
                  } else {
                    OK(APIResponse(ErrorRes(HTTP_UNAUTHORISED_401, Some("Unauthorised"), "Unauthorised").toJson.toString , HTTP_UNAUTHORISED_401))
                  }
                }
                case None => MAKE_ERROR("Password not registered. Could be a temporary account.")
              }
            } else {
              OK(APIResponse(ErrorRes(HTTP_CONFLICT_409, Some("Conflict"), "Account doesn't match records").toJson.toString , HTTP_CONFLICT_409))
            }
          }
          case KO(error) => {
            logger.error(error)
            OK(APIResponse(ErrorRes(HTTP_NOTFOUND_404, Some("Not Found"), "Account not found").toJson.toString , HTTP_NOTFOUND_404))
          }
        }
      }
      case KO(error) => {
        logger.error(error)
        OK(APIResponse(ErrorRes(HTTP_NOTFOUND_404, Some("Not Found"), "Application not found").toJson.toString , HTTP_NOTFOUND_404))
      }
    }
  }
}