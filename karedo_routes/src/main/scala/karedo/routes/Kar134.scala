package karedo.routes

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.entity.dao.{KO, OK, Result}
import karedo.entity.{DbUserAccount, DbUserApp, UserAccount, UserApp}
import org.slf4j.LoggerFactory
import spray.json._
import DefaultJsonProtocol._

/**
  * Created by pakkio on 10/3/16.
  */
trait Kar134 extends KaredoRoute {
  def nl2br(s: String) = s.replace("\n", "<br>")

  val logger = LoggerFactory.getLogger(classOf[Kar134])

  val dbUserApp = new DbUserApp {}
  val dbUserAccount = new DbUserAccount {}
  //case class Ad(url:String)
  //case class AdsReturned(List[Ad]=List())

  def getAdsFor(accountId: UUID): Result[String, String] = {
    OK(List("a", "b").toJson.toString)
  }


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


    else KO(Error("Not yet implemented"))


    //    } else {
    //      if UserApp.find( application_id ) != null {
    //        // Account mapped to application
    //
    //        if UserApp.find( application_id ) != account_id {
    //          // Account in Application is different to what is stored on device.
    //          // Should fix itself at Login
    //          local_account = UserApp.get(application_id)
    //          local_account_id = local_account.account_id
    //
    //          using local_account_id => get ads
    //
    //          return 205 Reset Content + ads
    //          /* Mobile App: 205 indicates old data. Reset it and Mark for 4b. Send Code screen */
    //
    //        } else {
    //          // The right user to application mapping
    //          local_account_id = UserApp.get( application_id )
    //          local_account = UserAccount.get( local_account_id )
    //
    //          if session_id == null OR UserSession.find( session_id ) == null AND UserSession.find( session_id ) != local_account_id {
    //            // User has never logged in
    //
    //            using local_account => get ads
    //
    //            return 206 Partial Content + ads + account_id
    //            /* Mobile App: 206 indicates user login has expired. Mark for 4a. Login screen */
    //
    //          } else {
    //            // User has never logged in
    //            using local_account_id => get ads
    //
    //            return 200 OK + ads + account_id
    //            /* Mobile App: 200 indicates everything is fine and the user is logged in */
    //          }
    //        }
    //      } else {
    //        // Account not mapped to Application
    //        // This is temporary account. Login will fix it.
    //        local_account = create new UserAccount()
    //        local_account_id = local_account.account_id
    //        UserApp.put( application_id, account_id )
    //
    //        return 201 Created + ads
    //        /* Mobile App: 201 indicates first time user. Mark for 4b. Send Code screen */
    //      }
    //    }


  }




  def anonymousCall(applicationId: String): Result[Error, APIResponse] = {
    val uapp = dbUserApp.getById(applicationId)
    if (uapp.isOK) {
      getAds(uapp, 200) // app already mapped to a valid account id
    }
    else {
      // Create a new userAccount and connect it to applicationId
      val emptyAccount = UserAccount()
      val uacct = dbUserAccount.insertNew(emptyAccount.id, emptyAccount)
      if (uacct.isOK) {

        val app = UserApp(id = applicationId, account_id = emptyAccount.id)
        val uNewApp = dbUserApp.insertNew(app.id, app)
        getAds(uNewApp, 201) // creating a new mapping

      }
      else KO(Error(s"Error ${uacct.err} while inserting new account"))
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


  def kar134 = {
    Route {

      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}


      path("account" / Segment / "ads") {
        accountId =>
          optionalHeaderValueByName("X_Identification") { deviceId =>
            get {
              parameters('p, 's ?, 'c ?) {
                (applicationId, sessionId, adCount) =>
                  doCall(
                    {
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
