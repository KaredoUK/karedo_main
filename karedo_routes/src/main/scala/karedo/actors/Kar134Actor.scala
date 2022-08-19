package karedo.actors

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import karedo.entity.{Channel, UserAccount, UserAd, UserApp}
import karedo.entity.dao.{KO, OK, Result}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import org.slf4j.LoggerFactory
import spray.json._

/**
  * Created by pakkio on 10/8/16.
  */


trait Kar134Actor extends KaredoCollections
  with DefaultJsonProtocol
  with SprayJsonSupport {
  val logger = LoggerFactory.getLogger(classOf[Kar134Actor])

  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
            deviceId: Option[String],
            applicationId: String,
            sessionId: Option[String],
            adCount: Option[String]): Result[Error, APIResponse] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nadCount: $adCount")


    if (accountId == "0") {
      anonymousCall(applicationId)
    }
    else {
      accountProvided(accountId, applicationId, sessionId)
    }
  }

  def getAds(uapp: Result[String, UserApp], code: Int): Result[Error, APIResponse] = {
    if (uapp.isOK) {
      val app = uapp.get
      val uAds = getAdsFor(app.account_id)
      if (uAds.isOK) {
        OK(APIResponse(uAds.get.toString, code))
      } else KO(Error(s"Can't get ads because of ${uAds.err}"))
    } else KO(Error(s"application cant be found because of ${uapp.err}"))
  }

  def getAdsFor(accountId: String): Result[String, String] = {

    OK {
      val list = dbUserAd.getAdsForUser("acctId")
      list.toJson.toString
    }
  }


  def anonymousCall(applicationId: String): Result[Error, APIResponse] = {
    val uapp = dbUserApp.getById(applicationId)
    if (uapp.isOK) {
      getAds(uapp, 200) // app already mapped to a valid account id
    }
    else {
      // Create a new userAccount and connect it to applicationId
      val emptyAccount = UserAccount()
      val uacct = dbUserAccount.insertNew(emptyAccount)
      if (uacct.isOK) {

        val app = UserApp(id = applicationId, account_id = emptyAccount.id)
        val uNewApp = dbUserApp.insertNew(app)
        getAds(uNewApp, 201) // creating a new mapping

      }
      else KO(Error(s"Error ${uacct.err} while inserting new account"))
    }


  }

  def accountProvided(accountId: String, applicationId: String, sessionId: Option[String]):
  Result[Error, APIResponse] = {
    val uapp = dbUserApp.getById(applicationId)
    if (uapp.isOK) {
      val storedAccountId = uapp.get.account_id
      if (accountId != storedAccountId) {
        getAds(uapp, 205)
      } else {
        val uacc = dbUserAccount.getById(accountId)
        if (sessionId.isDefined) {
          if (dbUserSession.getById(sessionId.get).isOK) {
            getAds(uapp, 200)
          } else {
            /* Mobile App: 206 indicates user login has expired. Mark for 4a. Login screen */
            getAds(uapp, 206)
          }
        } else {
          getAds(uapp, 206)
        }
      }
    } else {
      val uacc = dbUserAccount.insertNew(UserAccount())
      val uapp = dbUserApp.insertNew(UserApp(applicationId, uacc.get.id))
      if (uapp.isKO) {
        logger.error(s"can't insert mapping")
      }
      getAds(uapp, 201)
    }
  }

  implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

    val formatter = ISODateTimeFormat.basicDateTimeNoMillis

    def write(obj: DateTime): JsValue = {
      JsString(formatter.print(obj))
    }

    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s)
      }
      catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }

    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
    }
  }
  implicit val jsonChannel = jsonFormat2(Channel)
  implicit val jsonUserAd = jsonFormat10(UserAd)

}
