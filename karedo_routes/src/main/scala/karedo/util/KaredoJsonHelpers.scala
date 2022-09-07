package karedo.util

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity
import karedo.entity._
import karedo.rtb.model.AdModel.AdUnit
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

/**
  * Created by pakkio on 10/9/16.
  */
import karedo.util.DateTimeJsonHelper._

trait KaredoJsonHelpers
  extends DefaultJsonProtocol
  with SprayJsonSupport {

  case class ErrorRes(error_code: Int, error_type: Option[String], error_text: String)
  implicit val jsonErrorRes = jsonFormat3(ErrorRes)

  implicit val jsonChannel = jsonFormat2(Channel)
  implicit val jsonBacon = jsonFormat1(entity.Beacon)
  implicit val jsonImage = jsonFormat5(ImageAd)
  implicit val jsonVideo = jsonFormat6(VideoAd)
  implicit val jsonAd = jsonFormat13(Ad)
  implicit val jsonAds = jsonFormat2(Ads)

  implicit val jsonAction = jsonFormat2(Action)
  implicit val jsonMessage = jsonFormat6(UserMessages)


  case class Kar166Request(entries: List[UserAd] = List())
  implicit val jsonUserAd = jsonFormat10(UserAd)
  implicit val jsonKar166Request = jsonFormat1(Kar166Request)

  implicit val jsonUserProfile = jsonFormat12(UserProfile)

  case class Kar189ReqProfile(gender:Option[String], first_name:String, last_name: String,
                              yob: Option[Int], kids: Option[Int], income: Option[Int], location: Option[Boolean],
                              opt_in: Option[Boolean], third_party: Option[Boolean])
  case class Kar189Req(application_id: String, session_id: String, profile: Kar189ReqProfile )
  implicit val jsonKar189ReqProfile = jsonFormat9(Kar189ReqProfile)
  implicit val jsonKar189Req:RootJsonFormat[Kar189Req] = jsonFormat3(Kar189Req)

  implicit val jsonUserPrefs = jsonFormat4(UserPrefs)

  case class Kar195Req(application_id: String, session_id: String, prefs: Map[String, Double] )
  implicit val jsonKar195Req = jsonFormat3(Kar195Req)

  implicit val jsonIntent = jsonFormat6(IntentUnit)
  implicit val jsonUserIntent:RootJsonFormat[UserIntent] = jsonFormat2(UserIntent)

  case class Kar170ReqIntentUnit(why: String, what: String, when: String, where: String)
  case class Kar170Req(application_id: String, session_id: String, intent: Kar170ReqIntentUnit )

  implicit val jsonKar170ReqIntentUnit = jsonFormat4(Kar170ReqIntentUnit)
  implicit val jsonKar170Req:RootJsonFormat[Kar170Req] = jsonFormat3(Kar170Req)

  case class Kar172Req(application_id: String, session_id: String )
  implicit val jsonKar172Req = jsonFormat2(Kar172Req)

  case class Kar141_SendCode_Req(application_id: String, first_name: String, last_name: String,
                                 msisdn: String, user_type: String, email: String)
  implicit val jsonKar141_SendCode_Req = jsonFormat6(Kar141_SendCode_Req)

  case class Kar141_SendCode_Res(returning_user: Boolean, account_id: Option[String])
  implicit val jsonKar141_SendCode_Res = jsonFormat2(Kar141_SendCode_Res)

  case class SMSRequest(recipients: String, originator: String, body: String)
  implicit val jsonSMSRequest = jsonFormat3(SMSRequest)

  case class Kar145Req(application_id:String, msisdn:String, sms_code:String, password:String)
  implicit val jsonKar145Req = jsonFormat4(Kar145Req)

  case class Kar145Res(account_id:String)
  implicit val jsonKar145Res = jsonFormat1(Kar145Res)

  case class Kar135Res(account_id:Option[String], app_karedos:Int)
  implicit val jsonKar135Res = jsonFormat2(Kar135Res)

  case class Kar134Res(account_id:Option[String], ad_count:Int, ads:List[AdUnit] )
  implicit val jsonKar134Res = jsonFormat3(Kar134Res)

  case class Kar138Req(account_id: String, application_id:String, password: String)
  implicit val jsonKar138Req = jsonFormat3(Kar138Req)

  case class Kar138Res(session_id: String)
  implicit val jsonKar138Res = jsonFormat1(Kar138Res)

  case class Receiver(first_name: String, last_name: String, msisdn:String)
  case class Kar183Req(account_id: String, application_id: String, session_id: String, app_karedos:Int, receiver:Receiver )
  implicit val jsonReceiver = jsonFormat3(Receiver)
  implicit val jsonKar183Req:RootJsonFormat[Kar183Req] = jsonFormat5(Kar183Req)

  case class Kar197Req(account_id: String, application_id: String, session_id: String, app_karedos:Int )
  implicit val jsonKar197Req = jsonFormat4(Kar197Req)

  case class Kar197Res(sale_id: String)
  implicit val jsonKar197Res = jsonFormat1(Kar197Res)

  implicit val jsonSale = jsonFormat13(Sale)

  case class Kar199Res(qr_code: String)
  implicit val jsonKar199Res = jsonFormat1(Kar199Res)

  case class Kar186Req(account_id: String, application_id: String, session_id: String )
  implicit val jsonKar186Req = jsonFormat3(Kar186Req)

  case class MessageRes(message: String)
  implicit val jsonMessageRes = jsonFormat1(MessageRes)
}
