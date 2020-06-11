package parallelai.wallet.persistence.mongodb

import com.mongodb.casbah.commons.conversions.scala.{RegisterJodaTimeConversionHelpers, RegisterConversionHelpers}

trait MongoAppSupport {
  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()


}


