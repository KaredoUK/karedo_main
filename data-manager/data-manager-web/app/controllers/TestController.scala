package controllers

import api.{DataManagerRestClient, DataManagerApiClient}
import parallelai.wallet.config.AppConfigInjection
import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import play.api.mvc.Action._
import play.api.mvc.Results._
import play.mvc.Controller
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

  val TOUT = 10 seconds

  private def call00_clearDb() = {
    // remove previous accounts
    val coll=db("UserAccount")
    coll.remove(MongoDBObject.empty)
  }

  private def call01_RegisterNewApplication() = {
    val applicationId = UUID.randomUUID
    val request=RegistrationRequest(applicationId, msisdn = Some("0044734123456"), email = Some("name@email.com"))
    val response=Await.result(client.register(request), TOUT)
    // makes assertion on response
    applicationId
  }



  private def call02_ValidateRegistrationReturningAccountId(applicationId : UUID) : UUID = {
    // assert results
    // val ua = Await.result(userAccountDAO.getByApplicationId(applicationId), 10 seconds).get

    val ca = Await.result(clientApplicationDAO.getById(applicationId), TOUT).get
    val registration = RegistrationValidation(applicationId, ca.activationCode)
    val validated = Await.result(client.validateRegistration(registration),TOUT)
    validated.userID

  }

  private def call03_readProfile(accountId:UUID) : UserProfile = {

    Await.result(client.getUserProfile(accountId),TOUT).get

  }

  def index = async { implicit request =>
    call00_clearDb()
    val applicationId = call01_RegisterNewApplication
    val accountId = call02_ValidateRegistrationReturningAccountId(applicationId)
    val profile = call03_readProfile(accountId)


    val show = List(
      s"ApplicationId is $applicationId",
      s"AccountId is $accountId",
      s"Profile is $profile"
    )

    successful(Ok(views.html.test.render("Executed tests", show)))

  }

}

