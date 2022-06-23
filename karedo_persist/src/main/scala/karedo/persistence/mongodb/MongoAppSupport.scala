package karedo.persistence.mongodb

import com.escalatesoft.subcut.inject.Injectable
import com.mongodb.casbah.commons.conversions.scala.{RegisterJodaTimeConversionHelpers, RegisterConversionHelpers}

trait MongoAppSupport {
  self: Injectable =>

  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()

  setUserSessionExpiryIndex()

  // see: http://docs.mongodb.org/manual/tutorial/expire-data/
  def setUserSessionExpiryIndex(): Unit = {
    val userSessionDAO = new MongoUserSessionDAO()
    userSessionDAO.setSessionExpiryIndex()
  }

}


