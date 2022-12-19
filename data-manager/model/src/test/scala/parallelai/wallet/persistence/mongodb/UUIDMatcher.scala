package parallelai.wallet.persistence.mongodb

import org.specs2.matcher.Matcher

/**
 * Created on 21/03/2015.
 */
trait UUIDMatcher {
  val UUIDre="""^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".r

}
