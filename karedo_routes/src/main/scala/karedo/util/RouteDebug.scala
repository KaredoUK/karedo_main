package karedo.util

import akka.event.Logging._
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.RouteResult.Rejected
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.LogEntry
import akka.stream.scaladsl.Flow
import karedo.entity.{APIMessage, RequestMessage, ResponseMessage}
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import karedo.util.Util.now


/**
  * Created by pakkio on 10/3/16.
  */
trait RouteDebug {
  val routes: Route
  val logger = LoggerFactory.getLogger(classOf[RouteDebug])
  def routesWithLogging = logRequestResult(myLog _)(routes)

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }

  val dbColl = new DbCollections {
    def logEntry(request: String, response: String) = {
      import scala.concurrent.ExecutionContext.Implicits.global

      Future {
        dbAPIMessages.insertNew(
          APIMessage(
            id = s"${now}",
            request = RequestMessage(
              request = Some(request)
            ),
            response = ResponseMessage(
              response = Some(response)
            )
          ))
      }
    }
  }

  // logs just the request method and response status at info level
  def createLogEntry(request: HttpRequest, text: String): Some[LogEntry] = {

    dbColl.logEntry(request.toString, text)

    val logString: String = s"######\n[REQ]> ${request}\n=>\n[RES]> ${text}\n######"
//      "#### Request " + request + "\n => \n Response: " + text + "\n"
    logger.info(logString)
    Some(LogEntry(logString, InfoLevel))
  }

  def myLog(request: HttpRequest): Any => Option[LogEntry] = {
    case x: HttpResponse => {
      createLogEntry(request, x.toString)
//      x.entity match {
//        /*case e: HttpData => {
//            createLogEntry(request,   x.status + " " + e.asString)
//
//        }*/
//        case _ => createLogEntry(request, x.toString())
//      }
    } // log response
    case Rejected(rejections) => createLogEntry(request, " Rejection " + rejections.toString())
    case x => createLogEntry(request, x.toString())
  }
}
