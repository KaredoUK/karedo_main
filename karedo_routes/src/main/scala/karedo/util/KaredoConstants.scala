package karedo.util

import java.util.UUID

import karedo.actors.Error

/**
  * Created by crajah on 14/10/2016.
  */
trait KaredoConstants extends Configurable {
  val GENDER_MALE = "M"
  val GENDER_FEMALE = "F"

  val KAREDO_REVENUE_PERCENT = 0.80
  val USER_PERCENT =   .40

  val APP_KAREDO_CONV = 10.0

  val SMS_CODE_LENGTH = 10

  val TRANS_TYPE_CREATED = "CREATED"
  val TRANS_TYPE_TRANSFERED = "TRANSFERED"
  val TRANS_TYPE_SEND_RECEIVE = "SEND_RECEIVE"
  val TRANS_TYPE_SALE_APP = "SALE_APP"
  val TRANS_TYPE_REDEEM_PAYPAL = "REDEEM_PAYPAL"
  val TRANS_TYPE_REDEEM_BANK = "REDEEM_BANK"
  val TRANS_TYPE_REDEEM_CARD = "REDEEM_CARD"

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

  val DEFAULT_CUSTOMER_TYPE = "CUSTOMER"

  def getNewRandomID = UUID.randomUUID().toString

  def getNewSMSCode = {
    val random = new scala.util.Random
    random.alphanumeric take 6 mkString
  }

  def MAKE_ERROR(error:String, text:String = "") = KO(Error(s"$text \n\t---> $error"))

  val notification_base_url = conf.getString("notification.base.url")
  val notification_bcc_list = conf.getString("notification.bcc.list")

  val notification_email_auth_accesskey = conf.getString("notification.email.auth.accesskey")
  val notification_email_server_endpoint = conf.getString("notification.email.server.endpoint")
  val notification_email_sender = conf.getString("notification.email.sender")

  val notification_sms_auth_accesskey = conf.getString("notification.sms.auth.accesskey")
  val notification_sms_server_endpoint = conf.getString("notification.sms.server.endpoint")
  val notification_sms_sender = conf.getString("notification.sms.sender")


}
