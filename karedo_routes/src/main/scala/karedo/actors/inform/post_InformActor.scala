package karedo.actors.inform

import karedo.actors.{APIResponse, Error, KaredoAuthentication}
import karedo.entity._
import karedo.util.Util.now
import karedo.util._
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import scala.concurrent._
import ExecutionContext.Implicits.global

/**
  * Created by charaj on 25/01/2017.
  */
trait post_InformActor extends DbCollections
  with KaredoAuthentication
  with KaredoJsonHelpers
  with KaredoConstants
  with JiraHandler
{

  override val logger = LoggerFactory.getLogger(classOf[post_InformActor])

  def exec(request: post_InformRequest): Result[Error, APIResponse] = {

    try {
      val account_id = request.account_id match {
        case Some(a) => a
        case None => dbUserApp.find(request.application_id) match {
          case OK(userApp) => userApp.account_id
          case KO(_) => "_UNKNOWN_"
        }
      }

      // Insert to JIRA
      val jiraResponseFuture
      = jiraPostIssue(request.inform_type, request.subject, request.detail, request.image_base64)

      jiraResponseFuture.map(Success(_):Try[JiraIssueResponse]).recover{
        case t => Failure(t)
      }.map {
        case Success(s) => Inform(
          account_id = account_id,
          inform_type = request.inform_type,
          subject = request.subject,
          detail = request.detail,
          image_base64 = request.image_base64,
          jira = Some(
            Jira(
              s.id, s.key, s.self
            )
          )
        )
        case Failure(f) => {
          logger.error("Error Creating JIRA issue", f)
          Inform(
          account_id = account_id,
          inform_type = request.inform_type,
          subject = request.subject,
          detail = request.detail,
          image_base64 = request.image_base64
        )}
      }.map(i => {
        dbInform.insertNew(i) match {
          case KO(e) => logger.error(e)
          case _ =>
        }
      })
    } catch {
      case e:Exception => logger.error("Found Error in Inform", e)
    }

    OK(APIResponse("", HTTP_OK_200))
  }
}

