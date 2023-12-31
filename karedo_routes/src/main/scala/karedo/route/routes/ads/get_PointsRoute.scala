package karedo.route.routes.ads

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.route.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.persist.entity.{UserAccount, UserApp}
import karedo.route.common.{DbCollections, KaredoConstants, KaredoJsonHelpers}
import karedo.route.routes.KaredoRoute
import org.slf4j.LoggerFactory
import karedo.common.result.{Result, OK, KO}

import scala.util.{Failure, Success, Try}

object get_PointsRoute extends KaredoRoute
  with get_PointsActor {

  def route = {
    Route {

      // GET /account/{{account_id}}/points?p={{application_id}}&s={{session_id}}
      path("account" / Segment / "points") {
        accountId =>
          optionalHeaderValueByName(AUTH_HEADER_NAME) {
            deviceId =>
              get {
                parameters('p, 's ?) {
                  (applicationId, sessionId) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId)
                    }
                    )
                }
              }
          }
      }

    }

  }
}


trait get_PointsActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[get_PointsActor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String]): Result[Error, APIResponse] = {

    logger.debug(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false, respondAnyway = true)(
      (uApp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[Error, APIResponse]] {
          uAccount match {
            case OK(userAccount) => {
              val karedos = dbUserKaredos.find(userAccount.id) match {
                case OK(userKaredos) => userKaredos.karedos
                case KO(_) => 0
              }

              val app_karedos = karedos_to_appKaredos(karedos)
              val ret = KaredosResponse(JsonAccountIfNotTemp(userAccount), app_karedos).toJson.toString

              OK(APIResponse(ret, code))
            }
            case KO(_) => {
              uApp match {
                case OK(userApp) => {
                  val account_id = uApp.get.account_id
                  val karedos = dbUserKaredos.find(account_id) match {
                    case OK(userKaredos) => userKaredos.karedos
                    case KO(_) => 0
                  }

                  val app_karedos = karedos_to_appKaredos(karedos)
                  val userAccount = dbUserAccount.find(account_id).get
                  val ret = KaredosResponse(JsonAccountIfNotTemp(userAccount), app_karedos).toJson.toString

                  OK(APIResponse(ret, code))
                }
                case KO(e) => {
                  KO(Error(s"Application is not registered. Please call for /ads first. ${e}", code = code))
                }
              }
            }
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_THROWN_ERROR(f)
        }
        //
        //
        //        if (uAccount.isKO) KO(Error(s"internal error ${uAccount.err}"))
        //        else {
        //          val acc = uAccount.get
        //          val upoints = dbUserKaredos.find(acc.id)
        //
        //          if (upoints.isKO) KO(Error(s"internal error ${upoints.err}"))
        //          else {
        //            val app_karedos = karedos_to_appKaredos(upoints.get.karedos)
        //            val ret = Kar135Res(JsonAccountIfNotTemp(acc), app_karedos).toJson.toString
        //              OK(APIResponse(ret, code))
        //          }
        //        }
        //
        //
      }
    )
  }
}