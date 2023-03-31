package karedo.route.util

import karedo.persist.entity.{APIMessage, RequestMessage, ResponseMessage}

import scala.concurrent.Future
import karedo.common.misc.Util.now

import karedo.common.route.{RouteDebug => CommonRouteDebug}


/**
  * Created by pakkio on 10/3/16.
  */
trait RouteDebug extends CommonRouteDebug {

  val dbColl = new DbCollections {}

  override def logEntryString(source: String, request: String, response: String) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    Future {
      dbColl.dbAPIMessages.insertNew(
        APIMessage(
          id = s"${now}",
          request = RequestMessage(
            source = Some(source),
            request = Some(request)
          ),
          response = ResponseMessage(
            response = Some(response)
          )
        ))
    }
  }
}
