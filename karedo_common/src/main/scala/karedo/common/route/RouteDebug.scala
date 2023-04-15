package karedo.common.route

import akka.event.Logging.InfoLevel
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives.logRequestResult
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult.Rejected
import akka.http.scaladsl.server.directives.LogEntry
import akka.stream.scaladsl.Flow
import org.slf4j.LoggerFactory

import scala.concurrent.Future

/**
  * Created by charaj on 19/05/2017.
  */
trait RouteDebug {
  val routes: Route
  val logger = LoggerFactory.getLogger(classOf[RouteDebug])
  def routesWithLogging = logRequestResult(myLog _)(routes)

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
    case _ => TextMessage("Message type unsupported")
  }

  def logEntry(source: String, request: HttpRequest, response: HttpResponse) = {
    logEntryString(source, request.toString(), response.toString())
  }

  // @TODO: Needs to be implemented to database for analytics
  def logEntryString(source: String, request: String, response: String) = {}

  // logs just the request method and response status at info level
  def createLogEntry(source: String, request: HttpRequest, response: HttpResponse): Some[LogEntry] = {

    logEntry(source, request, response)

    createLogEntryString(source, request.toString, response.toString)
  }

  def createLogEntryString(source: String, request: String, response: String): Some[LogEntry] = {
    logEntryString(source, request, response)

    val logString: String = s"######\n[REQ]> ${request}\n=>\n[RES]> ${response}\n######"
    //      "#### Request " + request + "\n => \n Response: " + text + "\n"
    logger.info(logString)
    Some(LogEntry(logString, InfoLevel))
  }

  def myLog(request: HttpRequest): Any => Option[LogEntry] = {
    case response: HttpResponse => {
      createLogEntry("HttpResponse", request, response)
    } // log response
    case Rejected(rejections) => createLogEntryString("Rejection", request.toString(), " Rejection " + rejections.toString())
    case x => createLogEntryString("Other", request.toString, x.toString)
  }
}

