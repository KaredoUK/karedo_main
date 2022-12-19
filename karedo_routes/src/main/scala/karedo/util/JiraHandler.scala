package karedo.util

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{headers, _}

import scala.concurrent.Future
import spray.json._
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.http.scaladsl.unmarshalling.Unmarshal
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by charaj on 25/01/2017.
  */
trait JiraHandler extends Configurable with DefaultActorSystem {
  val jiraUrl = conf.getString("jira.url")
  val jiraUser = conf.getString("jira.user")
  val jiraPass = conf.getString("jira.pass")
  val jiraProject = conf.getString("jira.project")
  val jiraPrefix = conf.getString("jira.prefix")

  val logger = LoggerFactory.getLogger(classOf[JiraHandler])

  def jiraPostIssue(informType: String, subject: String, detail: Option[String], image_base64: Option[String]): Future[JiraIssueResponse]  = {
    val payload = JiraIssueRequest(
      JiraFields(
        JiraProject(
          key = Some(jiraProject)
        ), s"${jiraPrefix}${subject}", detail.getOrElse(subject),
        JiraIssueType(
          name = Some(TypeJiraIssue(informType).name)
        ),
        Some(List("CUSTOMER_DIRECT", informType))
      )
    )

    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = jiraUrl,
      entity = HttpEntity(ContentTypes.`application/json`, payload.toJson.toString),
      headers = List( headers.Authorization(BasicHttpCredentials(jiraUser, jiraPass)))
    )

    logger.debug("Outbound JIRA Request : " + request.toString)

    Http().singleRequest(request).flatMap {
      response => {
        logger.debug("Inbound JIRA Response : " + response)
        Unmarshal(response.entity).to[JiraIssueResponse]
      }
    }
  }

  case class JiraIssueRequest(fields: JiraFields)
  case class JiraFields(project: JiraProject, summary: String, description: String, issuetype: JiraIssueType, labels: Option[List[String]] = None)
  case class JiraProject(key: Option[String] = None, id: Option[String] = None)
  case class JiraIssueType(name: Option[String] = None, id: Option[String] = None)

  implicit val json_JiraIssueType = jsonFormat2(JiraIssueType)
  implicit val json_JiraProject = jsonFormat2(JiraProject)
  implicit val json_JiraFields = jsonFormat5(JiraFields)
  implicit val json_JiraIssueRequest:RootJsonFormat[JiraIssueRequest] = jsonFormat1(JiraIssueRequest)

  case class JiraIssueResponse(id: String, key: String, self: String)

  implicit val json_JiraIssueResponse:RootJsonFormat[JiraIssueResponse] = jsonFormat3(JiraIssueResponse)

  sealed trait TypeJiraIssue {
    def name:String
  }
  object TypeJiraIssue {
    def apply(typeName: String): TypeJiraIssue = {
      typeName match {
        case "BUG" => Bug
        case "SUGGEST" => Improvement
        case _ => Task
      }
    }
  }
  case object Improvement extends TypeJiraIssue{ override def name = "Improvement"}
  case object Bug extends TypeJiraIssue{ override def name = "Bug"}
  case object Task extends TypeJiraIssue{ override def name = "Task"}
}
