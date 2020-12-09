package controllers

import restapi.{DataManagerRestClient, DataManagerApiClient}
import org.joda.time.format.DateTimeFormat
import parallelai.wallet.config.AppConfigInjection
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import play.api.mvc.Action._
import play.api.mvc.Results._
import play.mvc.Controller
import org.joda.time.DateTime
import scala.concurrent.Await
import scala.concurrent.Future._
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.UUID
import scala.util.{Success, Failure}
import com.parallelai.wallet.datamanager.data._

// PK: added for unit testing
import scala.concurrent.duration.DurationInt
import parallelai.wallet.persistence.mongodb.{ClientApplicationMongoDAO, UserAccountMongoDAO}
import com.mongodb.casbah.Imports._


object TestController extends Controller with AppConfigInjection {
  implicit val _ = bindingModule

  val client: DataManagerApiClient = new DataManagerRestClient
  val userAccountDAO: UserAccountDAO = new UserAccountMongoDAO()
  val clientApplicationDAO: ClientApplicationDAO = new ClientApplicationMongoDAO()
  val mongoClient = MongoClient("localhost", 27017)
  val db = mongoClient("wallet_data")

  val TOUT = 10.seconds

  private def call00_clearDb() = {
    // remove previous accounts
    val coll=db("UserAccount")
    coll.remove(MongoDBObject.empty)
  }

  private def call01_RegisterNewApplication(phoneNr:Option[String],email:Option[String]) = {
    val applicationId = UUID.randomUUID
    //val request=RegistrationRequest(applicationId, msisdn = Some("00447909738629"), email = Some("name@email.com"))
    val request=RegistrationRequest(applicationId, msisdn = phoneNr, email = email)
    val response=Await.result(client.register(request), TOUT)
    // makes assertion on response
    applicationId
  }

  private def call02_ValidateRegistrationReturningAccountId(applicationId : UUID) : UUID = {
    // assert results
    // val ua = Await.result(userAccountDAO.getByApplicationId(applicationId), 10 seconds).get

    val ca = clientApplicationDAO.getById(applicationId).get
    val registration = RegistrationValidation(applicationId, ca.activationCode)
    val validated = Await.result(client.validateRegistration(registration),TOUT)
    validated.userID

  }

  private def call03_readProfile(accountId:UUID) : UserProfile = {

    Await.result(client.getUserProfile(accountId),TOUT).get

  }




  private def call04_changeProfile(accountId:UUID) = {

    val birthDate=DateTime.parse("04/02/2011", DateTimeFormat.forPattern("dd/MM/yyyy"))

    val info = UserInfo(accountId,"Claudio Pacchiega",Some("claudio.pacchiega@gmail.com"),Some("00447909738629"),Some("NW3 5ED"),Some("UK"),
      Some(birthDate), Some("M") )

    val settings = UserSettings(5)

    val userPoints=100

    val userProfile=UserProfile(info,settings,userPoints)
    Await.result(client.updateUserProfile(userProfile),TOUT)
    val read=Await.result(client.getUserProfile(accountId),TOUT).get

    read match {
      case UserProfile(ri,rs,up) =>
        val res = (if (ri==info) "" else " Info differs! ").concat(
           if(rs==settings) "" else " Settings differ! ").concat(
           if(up==userPoints) "" else " UserPoints differ! ")
        if (res!="") "Problems rereading userprofile "+res else "UserProfile correctly changed. OK"

      case _ => "Not found any userprofile"
    }




  }

  def index = async { implicit request =>
    call00_clearDb()
    val applicationId = call01_RegisterNewApplication(phoneNr=Some("00447909738629"),email=None)
    val accountId = call02_ValidateRegistrationReturningAccountId(applicationId)
    val profile = call03_readProfile(accountId)
    val changedProfile = call04_changeProfile(accountId)


    val show = List(
      s"- ApplicationId is $applicationId",
      s"- AccountId is $accountId",
      s"- Profile is $profile",
      s"- Changing Profile is $changedProfile"
    )

    successful(Ok(views.html.test.render("Executed tests", show)))

  }

}

