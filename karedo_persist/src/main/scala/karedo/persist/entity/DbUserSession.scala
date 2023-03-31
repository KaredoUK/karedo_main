package karedo.persist.entity

import java.util.UUID

import karedo.persist.entity.dao._
import karedo.route.util.{KO, Result}
import org.joda.time.DateTime
import salat.annotations._
import karedo.common.misc.Util.now

object UserSessionObj {
  val EXPIRY_MINUTES = 20
  val EXPIRY_DAYS = 1

  def expire = now.plusDays(EXPIRY_DAYS)
}

case class UserSession
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , account_id: String
  , ts_created: DateTime = now
  , ts_expire: DateTime = now.plusDays(UserSessionObj.EXPIRY_DAYS)
  , info: Option[String] = None
)
  extends Keyable[String]

trait DbUserSession extends DbMongoDAO_Casbah[String, UserSession] {
  override def find(id: String): Result[String, UserSession] = {
    val usess = super.find(id)
    // if not found not further action
    if (usess.isKO) usess
    else {
      val sess = usess.get
      if (sess.ts_expire.isBeforeNow) {
        val result = super.delete(sess)
        if (result.isKO) logger.error(s"cant remove expired sess ${result.err}")
        KO(s"Session Expired on ${sess.ts_expire}")
      } else {
        val result = super.update(sess.copy(ts_expire = UserSessionObj.expire))
        if (result.isKO) logger.error(s"cant remove expired sess ${result.err}")
        result
      }
    }
  }
}





