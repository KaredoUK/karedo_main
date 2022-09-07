package karedo.actors.sale

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity.{UserAccount, UserApp, UserProfile}
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory
import spray.json._
import scala.util.{Try, Success, Failure}

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar198_getSale_actor
  extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants {
  override val logger = LoggerFactory.getLogger(classOf[Kar198_getSale_actor])

  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           saleId: String): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId")

    authenticate(accountId, deviceId, applicationId, sessionId, allowCreation = false)(
      (uapp: Result[String, UserApp], uAccount: Result[String, UserAccount], code: Int) => {
        Try[Result[Error, APIResponse]] {
          dbSale.find(saleId) match {
            case OK(sale) => OK(APIResponse(sale.toJson.toString, HTTP_OK_200))
            case KO(error) => MAKE_ERROR(error)
          }
        } match {
          case Success(s) => s
          case Failure(f) => MAKE_ERROR(f)
        }
      }
    )
  }
}