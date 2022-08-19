package karedo.routes

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.entity.dao.{KO, OK, Result}
import karedo.entity._
import org.slf4j.LoggerFactory
import spray.json.{JsString, _}
import DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, ISODateTimeFormat}

/**
  * Created by pakkio on 10/3/16.
  */
trait Kar134 extends KaredoRoute {
  //def nl2br(s: String) = s.replace("\n", "<br>")

  val logger = LoggerFactory.getLogger(classOf[Kar134])

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

  def kar134 = {
    Route {

      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}


      path("account" / Segment / "ads") {
        accountId =>
          optionalHeaderValueByName("X_Identification") {
            deviceId =>
              get {
                parameters('p, 's ?, 'c ?) {
                  (applicationId, sessionId, adCount) =>
                    doCall({
                      exec(accountId, deviceId, applicationId, sessionId, adCount)
                    }
                    )
                }
              }
          }
      }

    }

  }
}
