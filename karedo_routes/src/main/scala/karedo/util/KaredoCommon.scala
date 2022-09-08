package karedo.util

import java.util.UUID

import karedo.actors.Error
import karedo.entity._

import scala.concurrent.Future
import karedo.util.Util.now

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


/**
  * Created by crajah on 14/10/2016.
  */
trait KaredoConstants extends Configurable {
  val GENDER_MALE = "M"
  val GENDER_FEMALE = "F"

  val KAREDO_REVENUE_PERCENT = 0.80
  val USER_PERCENT =   .40

  val APP_KAREDO_CONV:Long = 10

  val SMS_CODE_LENGTH = 10

  val TRANS_TYPE_CREATED = "CREATED"
  val TRANS_TYPE_TRANSFER = "TRANSFER"
  val TRANS_TYPE_SEND_RECEIVE = "SEND_RECEIVE"
  val TRANS_TYPE_SALE_APP = "SALE_APP"
  val TRANS_TYPE_REDEEM_PAYPAL = "REDEEM_PAYPAL"
  val TRANS_TYPE_REDEEM_BANK = "REDEEM_BANK"
  val TRANS_TYPE_REDEEM_CARD = "REDEEM_CARD"

  //             "deception": "Values are OPEN, FAILED, COMPLETE, EXPIRED, CANCELLED"

  val TRANS_STATUS_OPEN = "OPEN"
  val TRANS_STATUS_FAILED = "FAILED"
  val TRANS_STATUS_CLOSED = "CLOSED"
  val TRANS_STATUS_COMPLETE = "COMPLETE"
  val TRANS_STATUS_EXPIRED = "EXPIRED"
  val TRANS_STATUS_CANCELLED = "CANCELLED"

  val HTTP_OK_200 = 200
  val HTTP_OK_CREATED_201 = 201
  val HTTP_OK_ACCEPTED_202 = 202
  val HTTP_OK_NOCONTENT_204 = 204
  val HTTP_OK_RESETCONTENT_205 = 205
  val HTTP_OK_PARTIALCONTENT_NOTINASESSION_206 = 206
  val HTTP_BADREQUEST_400 = 400
  val HTTP_UNAUTHORISED_401 = 401
  val HTTP_FORBIDDEN_403 = 403
  val HTTP_NOTFOUND_404 = 404
  val HTTP_CONFLICT_409 = 409
  val HTTP_SERVER_ERROR_500 = 500

  val MIME_JSON = "JSON"
  val MIME_TEXT = "TEXT"
  val MIME_HTML = "HTML"

  val DEFAULT_CUSTOMER_TYPE = "CUSTOMER"

  val notification_base_url = conf.getString("notification.base.url")
  val notification_bcc_list = conf.getString("notification.bcc.list")

  val notification_email_auth_accesskey = conf.getString("notification.email.auth.accesskey")
  val notification_email_server_endpoint = conf.getString("notification.email.server.endpoint")
  val notification_email_sender = conf.getString("notification.email.sender")

  val notification_sms_auth_accesskey = conf.getString("notification.sms.auth.accesskey")
  val notification_sms_server_endpoint = conf.getString("notification.sms.server.endpoint")
  val notification_sms_sender = conf.getString("notification.sms.sender")

  val qr_base_url = conf.getString("qr.base.url")
  val qr_img_path = conf.getString("qr.img.path")
}

trait KaredoIds {
  def getNewRandomID = UUID.randomUUID().toString

  def getNewSMSCode = {
    val random = new scala.util.Random
    (random.alphanumeric take 6 mkString).toUpperCase
  }

  def getNewSaleCode = {
    val random = new scala.util.Random
    val first = (random.alphanumeric take 4 mkString).toUpperCase
    val second = (random.alphanumeric take 4 mkString).toUpperCase
    val third = (random.alphanumeric take 4 mkString).toUpperCase
    val fourth = (random.alphanumeric take 4 mkString).toUpperCase

    s"${first}-${second}-${third}-${fourth}"
  }
}

trait KaredoUtils
  extends DbCollections
    with KaredoConstants
  with KaredoIds
  with DefaultActorSystem
  with KaredoJsonHelpers
{

  def MAKE_ERROR(error:String, text:String = "") = KO(Error(s"$text \n\t---> $error"))

  def MAKE_ERROR(error:Throwable) = KO(Error(error.getMessage + " >> " + error.getStackTrace.foldLeft[String]("")((z, x) => z + " -> " + x.toString ) ))


  def moveKaredosBetweenAccounts
  (from_id: String, to_id: String, karedos: Option[Long], text: String = "", currency: String = "KAR"): Result[Error, String] = {
    val fromUserKaredo = dbUserKaredos.find(from_id).get
    val toUserKaredo = dbUserKaredos.find(to_id).get

    val act_karedo = karedos match {
      case Some(k) => k
      case None => {
        // All Karedos.
        fromUserKaredo.karedos
      }
    }

    if (fromUserKaredo.karedos < act_karedo) {
      MAKE_ERROR(s"From UserKaredos doesn't have enough Karedos accountId: ${from_id}")
    } else {

      val diff1 = fromUserKaredo.karedos - act_karedo
      val new_fromUserKaredo = fromUserKaredo.copy(karedos = diff1.toInt, ts = now)

      val diff2 = toUserKaredo.karedos + act_karedo
      val new_toUserKaredo = toUserKaredo.copy(karedos = diff2.toInt, ts = now)

      val updFromUserKaredo = dbUserKaredos.update(new_fromUserKaredo)

      if (updFromUserKaredo.isKO) MAKE_ERROR(updFromUserKaredo.err, s"Unable to update Karedos. accountId: $from_id")
      else {

        val updToUserKaredo = dbUserKaredos.update(new_toUserKaredo)
        if (updToUserKaredo.isKO) MAKE_ERROR(updToUserKaredo.err, s"Unable to update Karedos. accountId: $to_id")

        else {

          val insChangeFrom = dbKaredoChange.insertNew(KaredoChange(
            accountId = from_id, karedos = -act_karedo, trans_type = TRANS_TYPE_TRANSFER,
            trans_info = s"Moved Karedos: $karedos from $from_id to $to_id -> $text",
            trans_currency = currency, ts = now))
          if (insChangeFrom.isKO) MAKE_ERROR(insChangeFrom.err, "Unable to update KaredoChange")
          else {

            val insChangeTo = dbKaredoChange.insertNew(KaredoChange(
              accountId = to_id, karedos = act_karedo, trans_type = TRANS_TYPE_TRANSFER,
              trans_info = s"Moved Karedos: $karedos from $from_id to $to_id -> $text",
              trans_currency = currency, ts = now))
            if (insChangeTo.isKO) MAKE_ERROR(insChangeTo.err, "Unable to update KaredoChange")
            else
              OK("Complete")
          }
        }
      }
    }


  }

  def karedos_to_appKaredos(karedos: Long): Int = {
    (karedos / APP_KAREDO_CONV).toInt
  }

  def appKaredos_to_karedos(app_karedos: Int): Long = {
    (app_karedos * APP_KAREDO_CONV).toLong
  }


  def sendSMS(msisdn: String, text: String): Future[Result[Error, String]] = {
    import spray.client.pipelining._
    import spray.http._

    val pipeline: HttpRequest => Future[HttpResponse] = {
      addHeader("Authorization", s"AccessKey $notification_sms_auth_accesskey") ~> sendReceive //~> unmarshal[String]
    }

    pipeline {
      Post(
        Uri(notification_sms_server_endpoint), SMSRequest(msisdn, notification_sms_sender, text).toJson.toString)
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        MAKE_ERROR(s"Request failed for reason ${httpResponse.status.value}:${httpResponse.status.defaultMessage}")
      } else {
        OK(s"[SMS] Sent a sms, response from service is $httpResponse")
      }
    }
  }

  def sendEmail(email: String, subject: String, body: String): Future[Result[Error, String]] = {
    import spray.client.pipelining._
    import spray.http.{BasicHttpCredentials, FormData, HttpResponse}

    val requestPipeline = addCredentials(BasicHttpCredentials("api", s"$notification_email_auth_accesskey")) ~> sendReceive

    requestPipeline {
      Post(
        notification_email_server_endpoint,
        FormData(
          Map(
            "from" -> notification_email_sender,
            "to" -> email,
            "subject" -> subject,
            "html" -> body
          )
        )
      )
    } map { httpResponse: HttpResponse =>
      if (httpResponse.status.isFailure) {
        MAKE_ERROR(s"[EMAIL] Got an error response is ${httpResponse.entity.asString}")
      } else {
        OK(s"[EMAIL] Email sent correctly answer is ${httpResponse.entity}")
      }
    }
  }

  def createAndInsertNewAccount(account_id: String): Result[String, UserAccount] = {
    Try {
      val ret = dbUserAccount.insertNew(
        UserAccount(account_id, None, None, DEFAULT_CUSTOMER_TYPE, List(), List(), true, now, now)
      )

      dbUserKaredos.insertNew(UserKaredos(account_id, 0, now))
      dbUserProfile.insertNew(UserProfile(id = account_id))

      val pref_map = getDefaultPrefMap

      dbUserPrefs.insertNew(UserPrefs(id = account_id, prefs = pref_map))
      dbUserIntent.insertNew(UserIntent(id = account_id))
      dbUserMessages.insertNew(UserMessages(getNewRandomID, account_id,
        Some("Welcome to Karedo"), Some("Welcome to Karedo"), Some("Welcome to Karedo")) )

      ret
    } match {
      case Success(s) => s
      case Failure(f) => KO(f.toString)
    }
  }

  def getDefaultPrefMap():Map[String, UserPrefData] = {
    val prefMap = dbPrefs.load.map(x => x.id -> UserPrefData(x.default, x.name, x.order))(collection.breakOut): Map[String, UserPrefData]

    sortPrefMap(prefMap.filter(_._2.include))
  }

  def sortPrefMap(prefMap:Map[String, UserPrefData]): Map[String, UserPrefData] = {
    ListMap(prefMap.toSeq.sortWith(_._2.order < _._2.order):_*)
  }

}

