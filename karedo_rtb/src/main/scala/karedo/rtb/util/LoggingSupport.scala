package karedo.rtb.util

import org.slf4j.LoggerFactory

/**
  * Created by crajah on 04/12/2016.
  */
trait LoggingSupport {
  val logger = LoggerFactory.getLogger(classOf[LoggingSupport])
}
