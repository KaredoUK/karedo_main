package karedo.util

import java.io.File
import java.util.UUID
import javax.imageio.ImageIO

import akka.http.scaladsl.model.HttpHeader
import com.google.zxing.client.j2se.{BufferedImageLuminanceSource, MatrixToImageConfig, MatrixToImageWriter}
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.{BarcodeFormat, BinaryBitmap, EncodeHintType}
import com.google.zxing.qrcode._
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.QRCode
import com.sun.javafx.iio.ImageStorage.ImageType

import scala.collection.{JavaConversions, mutable}
import scala.collection.JavaConverters._
import karedo.actors.Error
import karedo.entity._

import scala.concurrent.Future
import karedo.util.Util.now
import org.slf4j.LoggerFactory

import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.tools.nsc.io.Path
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

  val HTTP_REDIRECT_302 = 302

  val HTTP_BADREQUEST_400 = 400
  val HTTP_UNAUTHORISED_401 = 401
  val HTTP_FORBIDDEN_403 = 403
  val HTTP_NOTFOUND_404 = 404
  val HTTP_CONFLICT_409 = 409
  val HTTP_GONE_410 = 410
  val HTTP_SERVER_ERROR_500 = 500

  val MIME_JSON = "JSON"
  val MIME_TEXT = "TEXT"
  val MIME_HTML = "HTML"

  val COOKIE_ACCOUNT = "_a"

  val AD_TYPE_IMAGE = "IMAGE"
  val AD_TYPE_VIDEO = "VIDEO"
  val AD_TYPE_IMAGE_NATIVE = "IMAGE_NATIVE"
  val AD_TYPE_VIDEO_NATIVE = "VIDEO_NATIVE"

  /* Choices {FACEBOOK, TWITTER, GOOGLE+, INTSAGRAM, EMAIL} */
  val SOCIAL_FACEBOOK = "FACEBOOK"
  val SOCIAL_TWITTER = "TWITTER"
  val SOCIAL_GOOGLE_P = "GOOGLE+"
  val SOCIAL_INSTAGRAM = "INSTAGRAM"
  val SOCIAL_EMAIL = "EMAIL"

  val DEFAULT_CUSTOMER_TYPE = "CUSTOMER"

  val GET_TERM = "TERMS"
  val GET_ABOUT = "ABOUT"
  val GET_PRIVACY = "PRIVACY"

  val QR_SIZE = 300

  val notification_base_url = conf.getString("notification.base.url")
  val notification_bcc_list = conf.getString("notification.bcc.list")

  val notification_email_auth_accesskey = conf.getString("notification.email.auth.accesskey")
  val notification_email_server_endpoint = conf.getString("notification.email.server.endpoint")
  val notification_email_sender = conf.getString("notification.email.sender")

  val notification_sms_auth_accesskey = conf.getString("notification.sms.auth.accesskey")
  val notification_sms_server_endpoint = conf.getString("notification.sms.server.endpoint")
  val notification_sms_sender = conf.getString("notification.sms.sender")

  val qr_base_file_path = conf.getString("qr.base.file.path")
  val qr_img_file_path = conf.getString("qr.img.file.path")
  val qr_base_img_url = conf.getString("qr.base.img.url")

  val url_magic_share_base = conf.getString("url.magic.share.base")
  val url_magic_norm_base = conf.getString("url.magic.norm.base")
  val url_magic_fallback_url = conf.getString("url.magic.fallback.url")

  def getDeviceType(make: Option[String], model: Option[String]): Int = {
    val DEVICE_TYPE_MOBILE_TABLET: Int = 1
    val DEVICE_TYPE_PC: Int = 2
    val DEVICE_TYPE_TV: Int = 3
    val DEVICE_TYPE_PHONE: Int = 4
    val DEVICE_TYPE_TABLET: Int = 5
    val DEVICE_TYPE_DEVICE: Int = 6
    val DEVICE_TYPE_STB: Int = 7

    implicit class StringInterpolations(sc: StringContext) {
      def ic = new {
        def unapply(other: String) = sc.parts.mkString.equalsIgnoreCase(other)
      }
    }

    (make, model) match {
      case (Some(ic"Apple"), None) => DEVICE_TYPE_MOBILE_TABLET
      case (Some(ic"Apple"), Some(ic"iPhone")) => DEVICE_TYPE_PHONE
      case (Some(ic"Apple"), Some(ic"iPad")) => DEVICE_TYPE_TABLET
      case (Some(ic"Android"), None) => DEVICE_TYPE_MOBILE_TABLET
      case _ => DEVICE_TYPE_MOBILE_TABLET
    }
  }
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

  def getSHA1Hash(s: String): String = {
    java.security.MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8")).map("%02x".format(_)).mkString
  }
}

trait KaredoUtils
  extends DbCollections
    with KaredoConstants
  with KaredoIds
  with DefaultActorSystem
  with KaredoJsonHelpers
{
  def MAKE_THROWABLE(error:String, text:String = "", code: Int = 500, mime:String = "", headers:List[HttpHeader] = List()) = {
    Error(ErrorInfo(error, text).toJson.toString, code, mime, headers)
  }

  def MAKE_ERROR(error:String, text:String = "", code: Int = 500, mime:String = "", headers:List[HttpHeader] = List()) = {
    KO(MAKE_THROWABLE(error, text, code, mime, headers))
  }

  def MAKE_THROWN_ERROR(error:Throwable, code: Int = 500, mime:String = "", headers:List[HttpHeader] = List()) = {
    val message = error.getMessage
    val stack = error.getStackTrace.map(_.toString).toList

    MAKE_ERROR(message, ErrorStack(stack).toJson.toString, code, mime, headers)
  }


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

      val completed = dbUserKaredos.transferKaredo(from_id, to_id, act_karedo, "TRANSFER", text, currency)
      if(completed.isKO){
        MAKE_ERROR(completed.err, "Unable to transfer karedos")
      } else {
        OK("Complete")
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

      dbUserKaredos.insertNew(UserKaredos(account_id, 0))
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

  def storeUrlMagic(first_url:String, second_url:Option[String]):Result[String, String] = {
    val url = second_url match {
      case Some(second_url) => s"${first_url}_${second_url}"
      case None => first_url
    }

    val url_code = getSHA1Hash(url)

    dbUrlMagic.find(url_code) match {
      case KO(_) => dbUrlMagic.insertNew(UrlMagic(url_code, first_url, second_url, now)) match {
        case OK(_) => OK(url_code)
        case KO(_) => KO(url_code)
      }
      case OK(_) => OK(url_code)
    }
  }

  def storeAccountHash(account_id: String): Result[String, String] = {
    val account_hash = getSHA1Hash(account_id)

    Try[Result[String, HashedAccount]] {
      dbHashedAccount.find(account_hash) match {
        case KO(_) => dbHashedAccount.insertNew(HashedAccount(account_hash, account_id))
        case OK(_) => OK(HashedAccount(account_hash, account_id))
      }
    } match {
      case Success(s) => OK(account_hash)
      case Failure(f) => {
        val logger = LoggerFactory.getLogger(classOf[KaredoUtils])
        logger.error("Hashing Account Failed", f)
        OK(account_hash)
      }
    }
  }
}

trait KaredoQRCode extends KaredoConstants {
  val logger = LoggerFactory.getLogger(classOf[KaredoQRCode])

  def getQRCode(text: String): Result[Error, String] = {
    try {

      val basePath = qr_base_file_path + File.separator + qr_img_file_path
      val basePathFile = new File(basePath)
      val baseUrl = qr_base_img_url + File.separator + qr_img_file_path

      if( ! basePathFile.exists() ) basePathFile.mkdirs()

      val qrw = new QRCodeWriter()
      val hints: scala.collection.mutable.Map[EncodeHintType, Any] = scala.collection.mutable.Map()

      hints += (EncodeHintType.ERROR_CORRECTION -> ErrorCorrectionLevel.H)
      hints += (EncodeHintType.CHARACTER_SET -> "utf-8")

      val qr_bitMatrix = qrw.encode(text, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints.asJava)

      val qrFile = new File(basePath + File.separator + text + ".png")
      qrFile.createNewFile()
      MatrixToImageWriter.writeToPath(qr_bitMatrix, "png", qrFile.toPath(), new MatrixToImageConfig())

      OK(baseUrl + File.separator + text + ".png")
    } catch {
      case ex: Exception => {
        logger.error("Couldn't create QR Code", ex)
        KO(Error("Couldn't create QR Code: " + ex.toString))
      }
    }
  }

  def decodeQRCode(file: File): Result[Error, String] = {
    try {
      val image = ImageIO.read(file)
      val lumSource = new BufferedImageLuminanceSource(image)
      val bitmap = new BinaryBitmap(new HybridBinarizer(lumSource))

      val qrr = new QRCodeReader()
      val res = qrr.decode(bitmap)

      OK(res.getText)

    } catch {
      case ex:Exception => {
        logger.error("Couldn't decode QR Code", ex)
        KO(Error("Couldn't decode QR Code: " + ex.toString))
      }
    }
  }
}

