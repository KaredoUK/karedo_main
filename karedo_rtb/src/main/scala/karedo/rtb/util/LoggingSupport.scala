package karedo.rtb.util

import org.slf4j.{LoggerFactory, MarkerFactory}

/**
  * Created by crajah on 04/12/2016.
  */
trait LoggingSupport {
  val logger = LoggerFactory.getLogger(classOf[LoggingSupport])

  val marker = MarkerFactory.getMarker("AD_REQUEST")
}

