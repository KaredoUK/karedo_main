package karedo.routes

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import karedo.entity.{DbUserAccount, DbUserApp, UserAccount, UserApp}
import karedo.util.objAPI.{APIError, APIOK, InternalError}
import karedo.util.{FailureResponse, ResponseWithFailure, SuccessResponse}
import org.slf4j.LoggerFactory

/**
  * Created by pakkio on 10/3/16.
  */
trait Kar134 {
  def nl2br(s: String) = s.replace("\n", "<br>")

  val logger = LoggerFactory.getLogger(classOf[Kar134])

  val dbUserApp = new DbUserApp {}
  val dbUserAccount = new DbUserAccount {}



  def getAdsFor(accountId: UUID): Either[String, List[String]] = {
    Right(List("a", "b"))
  }


  // exec will be moved to proper actor (or stream in business logic layer)
  def exec(accountId: String,
           deviceId: Option[String],
           applicationId: String,
           sessionId: Option[String],
           adCount: Option[String]): ResponseWithFailure[APIError,APIOK] = {

    logger.info(s"OK\nAccountId: $accountId\ndeviceId: $deviceId\napplicationId: $applicationId\nsessionId: $sessionId\nadCount: $adCount")


    if (accountId == "0") {
      anonymousCall(applicationId)
    }


      else FailureResponse(InternalError(msg ="Not yet implemented"))


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

  def isOk[String,T](t: Either[String,T]) = t.isRight
  def get[String,T](t: Either[String,T]) = t.right
  def err[String,T](t: Either[String,T]) = t.left


  def anonymousCall(applicationId:String): ResponseWithFailure[APIError,APIOK] = {
    val uapp = dbUserApp.getById(applicationId)
    if(isOk(uapp))
    (dbUserApp.getById(applicationId) match {
      case ret@ok(app) => ret

      case Left(x) =>
        // Create a new userAccount and connect it to applicationId
        val emptyAccount = UserAccount()
        dbUserAccount.insertNew(emptyAccount.id, emptyAccount) match {
          case Right(_) =>
            val app = UserApp(id = "appId2", account_id = emptyAccount.id)
            dbUserApp.insertNew(app.id, app) match {
              case Right(_) => Right(app)
              case Left(error) => Left(s"Cant insert $app because of $error")
            }
          case Left(x) => Left(s"Error $x while inserting new account")
        }
    }) match {

      case Right(app:UserApp) =>
        getAdsFor(app.account_id) match {
          case Right(x: List[String]) => SuccessResponse(APIOK(x.toString(),201))
          case Left(error) => FailureResponse(InternalError(new Exception(error)))
        }
      case Left(error) => FailureResponse(InternalError(msg = error))
    }
  }


  def kar134 = {
    Route(

      // GET /account/{{account_id}}/ads?p={{application_id}}&s={{session_id}}&c={{ad_count}}


      path("account" / Segment / "ads") {
        accountId =>
          optionalHeaderValueByName("X_Identification") { deviceId =>
            get {
              parameters('p, 's ?, 'c ?) {
                (applicationId, sessionId, adCount) => complete(
                  // exec(accountId, deviceId, applicationId, sessionId, adCount) match {
                  "OK"

                )

              }
            }
          }
      }
    )
  }
}
