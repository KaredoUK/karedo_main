package karedo.util

import karedo.rtb.model.AdModel.AdUnit
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

/**
  * Created by pakkio on 10/9/16.
  */
import karedo.util.DateTimeJsonHelper._
import karedo.entity._

trait KaredoJsonHelpers
  extends DefaultJsonProtocol
  with SprayJsonSupport {

  case class ErrorRes(error_code: Int, error_type: Option[String], error_text: String)
  implicit val jsonErrorRes = jsonFormat3(ErrorRes)

  case class ErrorStack(stack: List[String])
  implicit val jsonErrorStack = jsonFormat1(ErrorStack)

  case class ErrorInfo(message:String, info: String)
  implicit val jsonErrorInfo = jsonFormat2(ErrorInfo)

//  implicit val jsonChannel = jsonFormat2(Channel)
//  implicit val jsonBacon = jsonFormat1(entity.Beacon)
//  implicit val jsonImage = jsonFormat5(ImageAd)
//  implicit val jsonVideo = jsonFormat6(VideoAd)
//  implicit val jsonAd = jsonFormat13(Ad)
//  implicit val jsonAds = jsonFormat2(Ads)

//  implicit val jsonAction = jsonFormat2(Action)
//  implicit val jsonMessage = jsonFormat6(UserMessages)


//  implicit val jsonUserAd = jsonFormat10(UserAd)

  case class post_InteractionRequest(application_id: String, session_id:String, entries: List[InteractUnit] = List())

//  implicit val jsonChannelUnit = jsonFormat4(ChannelUnit)
//  implicit val jsonInteractUnit = jsonFormat10(InteractUnit)
  implicit val jsonKar166Request:RootJsonFormat[post_InteractionRequest] = jsonFormat3(post_InteractionRequest)

  case class post_ShareDataRequest(application_id: String, session_id:String, share: InteractUnit)
  implicit val jsonKar167Request:RootJsonFormat[post_ShareDataRequest] = jsonFormat3(post_ShareDataRequest)

  case class SocialChannelListResponse(channels: List[ChannelUnit])
  implicit val jsonKar167Res:RootJsonFormat[SocialChannelListResponse] = jsonFormat1(SocialChannelListResponse)

//  implicit val jsonFavouriteUnit = jsonFormat6(FavouriteUnit)

  case class post_FavouriteRequest(application_id: String, session_id:String, favourites: List[FavouriteUnit])
  implicit val jsonKar165Request:RootJsonFormat[post_FavouriteRequest] = jsonFormat3(post_FavouriteRequest)

  case class FavouriteListResponse(favourites: List[FavouriteUnit])
  implicit val jsonKar165Res:RootJsonFormat[FavouriteListResponse] = jsonFormat1(FavouriteListResponse)


//  implicit val jsonUserProfile = jsonFormat13(UserProfile)

  case class ProfileUnit(gender:Option[String], first_name:Option[String], last_name: Option[String],
                         yob: Option[Int], kids: Option[Int], income: Option[Int], postcode: Option[String], location: Option[Boolean],
                         opt_in: Option[Boolean], third_party: Option[Boolean])
  case class post_ProfileRequest(application_id: String, session_id: String, profile: ProfileUnit )
  implicit val jsonKar189ReqProfile = jsonFormat10(ProfileUnit)
  implicit val jsonKar189Req:RootJsonFormat[post_ProfileRequest] = jsonFormat3(post_ProfileRequest)

  case class post_ChangePasswordRequest(application_id: String, session_id: String, account_id: String,
                                        old_password: String, new_password: String)
  implicit val json_ChangePasswordRequest:RootJsonFormat[post_ChangePasswordRequest] = jsonFormat5(post_ChangePasswordRequest)

//  implicit val jsonUserPrefData = jsonFormat4(UserPrefData)
//  implicit val jsonUserPrefs:RootJsonFormat[UserPrefs] = jsonFormat4(UserPrefs)

  case class PrefsListResponse(prefs:Map[String, UserPrefData])
  implicit val jsonKar194Res_Prefs:RootJsonFormat[PrefsListResponse] = jsonFormat1(PrefsListResponse)

  case class post_PrefsRequest(application_id: String, session_id: String, prefs: Map[String, Double] )
  implicit val jsonKar195Req = jsonFormat3(post_PrefsRequest)

//  implicit val jsonIntent = jsonFormat6(IntentUnit)
//  implicit val jsonUserIntent:RootJsonFormat[UserIntent] = jsonFormat2(UserIntent)

  case class IntentUnit(why: String, what: String, when: String, where: String)
  case class IntentUpdateRequest(application_id: String, session_id: String, intent: IntentUnit )

  implicit val jsonKar170ReqIntentUnit = jsonFormat4(IntentUnit)
  implicit val jsonKar170Req:RootJsonFormat[IntentUpdateRequest] = jsonFormat3(IntentUpdateRequest)

  case class delete_IntentRequest(application_id: String, session_id: String )
  implicit val jsonKar172Req = jsonFormat2(delete_IntentRequest)

  case class post_SendCodeRequest(application_id: String, first_name: String, last_name: String,
                                  msisdn: String, user_type: String, email: String)
  implicit val jsonKar141_SendCode_Req = jsonFormat6(post_SendCodeRequest)

  case class delete_AccountRequest(application_id: String, account_id: String, session_id: String)
  implicit val jsonKar141_DeleteAccount_Req = jsonFormat3(delete_AccountRequest)

  case class SendCodeResponse(returning_user: Boolean, account_id: Option[String])
  implicit val jsonKar141_SendCode_Res = jsonFormat2(SendCodeResponse)

  case class SMSRequest(recipients: String, originator: String, body: String)
  implicit val jsonSMSRequest = jsonFormat3(SMSRequest)

  case class post_EnterCodeRequest(application_id:String, msisdn:String, sms_code:String, password:String)
  implicit val jsonKar145Req = jsonFormat4(post_EnterCodeRequest)

  case class AccountIdResponse(account_id:String)
  implicit val jsonKar145Res = jsonFormat1(AccountIdResponse)

  case class KaredosResponse(account_id:Option[String], app_karedos:Int)
  implicit val jsonKar135Res = jsonFormat2(KaredosResponse)

  case class AdResponse(account_id:Option[String], ad_count:Int, ads:List[AdUnit] )
  implicit val jsonKar134Res = jsonFormat3(AdResponse)

  case class post_LoginRequest(account_id: String, application_id:String, password: String)
  implicit val jsonKar138Req = jsonFormat3(post_LoginRequest)

  case class SessionIdResponse(session_id: String)
  implicit val jsonKar138Res = jsonFormat1(SessionIdResponse)

  case class Receiver(first_name: String, last_name: String, msisdn:String)
  case class put_TransferRequest(account_id: String, application_id: String, session_id: String, app_karedos:Int, receiver:Receiver )
  implicit val jsonReceiver = jsonFormat3(Receiver)
  implicit val jsonKar183Req:RootJsonFormat[put_TransferRequest] = jsonFormat5(put_TransferRequest)

  case class put_SaleRequest(account_id: String, application_id: String, session_id: String, app_karedos:Int )
  implicit val jsonKar197Req = jsonFormat4(put_SaleRequest)

  case class SaleIdResponse(sale_id: String)
  implicit val jsonKar197Res = jsonFormat1(SaleIdResponse)

//  implicit val jsonSale = jsonFormat13(Sale)

  case class QRCodeResponse(qr_code: String)
  implicit val jsonKar199Res = jsonFormat1(QRCodeResponse)

  case class post_SaleRequest(account_id: String, application_id: String, session_id: String )
  implicit val jsonKar186Req = jsonFormat3(post_SaleRequest)

  case class MessageRes(message: String)
  implicit val jsonMessageRes = jsonFormat1(MessageRes)

  case class put_ResendRequest(application_id: String, msisdn: String)
  implicit val jsonKar147_Resend = jsonFormat2(put_ResendRequest)

  case class put_ResendEmailRequest(application_id: String, email: String)
  implicit val jsonKar147_ResetEmail = jsonFormat2(put_ResendEmailRequest)

  case class post_ValidateEmailRequest(application_id: String, account_id: String, session_id: String, email: String)
  implicit val jsonKar147_ValidateEmail_Request = jsonFormat4(post_ValidateEmailRequest)

  case class ValidateBooleanResponse(valid: Boolean)
  implicit val jsonKar147_ValidateEmail_Res = jsonFormat1(ValidateBooleanResponse)

  case class UrlCodeAndAccountHash(url_code: String, account_hash: String)

  case class post_InformRequest
  (
    application_id: String
    , account_id: Option[String]
    , inform_type: String
    , subject: String
    , detail: Option[String]
    , image_base64: Option[String]
   )
  implicit val json_Kar12Req = jsonFormat6(post_InformRequest)
}
