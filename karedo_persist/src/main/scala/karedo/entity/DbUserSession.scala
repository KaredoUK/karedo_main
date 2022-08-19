package karedo.entity

import java.util.UUID

import karedo.entity.dao._
import org.joda.time.DateTime
import salat.annotations._
import karedo.entity.dao.Util.now

object UserSession {
  val EXPIRY_MINUTES = 20

  def expire = now.plusMinutes(EXPIRY_MINUTES)
}

case class UserSession
(
  @Key("_id") id: String = UUID.randomUUID().toString
  , account_id: String
  , ts_created: DateTime = now
  , ts_expire: DateTime = now.plusMinutes(UserSession.EXPIRY_MINUTES)
  , info: Option[String] = None
)
  extends Keyable[String]

trait DbUserSession extends DbMongoDAO[String, UserSession] {
  override def getById(id: String): Result[String, UserSession] = {
    val usess = super.getById(id)
    // if not found not further action
    if (usess.isKO) usess
    else {
      val sess = usess.get
      if (sess.ts_expire.isBeforeNow) {
        val result = super.delete(sess)
        if (result.isKO) logger.info(s"cant remove expired sess ${result.err}")
        KO(s"Session Expired on ${sess.ts_expire}")
      } else {
        val result = super.update(sess.copy(ts_expire = UserSession.expire))
        if (result.isKO) logger.error(s"cant remove expired sess ${result.err}")
        result
      }
    }
  }
}





