package cassandra

import org.scalatest.{BeforeAndAfterEach, Matchers, FlatSpec, Suite}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import parallelai.wallet.persistence.cassandra._
import java.util.UUID
import parallelai.wallet.entity.UserAccount
import scala.concurrent._
import scala.concurrent.duration._


trait EmbeddedCassandra extends BeforeAndAfterEach { self: Suite =>
  override def beforeEach() {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra()
    super.beforeEach() // To be stackable, must call super.beforeEach
  }

  override def afterEach() {
    try super.afterEach() // To be stackable, must call super.afterEach
    finally EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }
}

class UserAccountCassandraSpec extends FlatSpec with Matchers with EmbeddedCassandra {

  val futureTimeout = 4.seconds

  val dao = new UserAccountCassandraDAO()

  val userAccountRecord = new UserAccountRecord
  val clientApplicationRecord = new ClientApplicationRecord
  val userByEmail = new EmailUserLookupRecord
  val userByMsisdn = new MsisdnUserLookupRecord

  implicit val session = userAccountRecord.cassandra

  behavior of "ClientApplicationCassandraDAO"
  
  it should "retrieve a user by ApplicationID" in {
    val applicationId = UUID.randomUUID()
    val userAccount = UserAccount(UUID.randomUUID(), None, None)

    clientApplicationRecord.insert.value( _.id, applicationId ).value(_.accountId, userAccount.id)
    userAccountRecord.insert.value(_.id, userAccount.id)

    val retrievedAccount = Await.result( dao.getByApplicationId(applicationId), futureTimeout)

    retrievedAccount should be (defined)
    retrievedAccount.get.id should equal (userAccount.id)
  }

  it should "retrieve a user by email" in {
    val userEmail = "user@email.com"
    val userAccount = UserAccount(UUID.randomUUID(), None, Some(userEmail))

    userByEmail.insert.value(_.email, userEmail)
    userAccountRecord.insert.value(_.id, userAccount.id).value(_.email, userAccount.email)

    val retrievedAccount = Await.result( dao.getByEmail(userEmail), futureTimeout)

    retrievedAccount should be (defined)
    retrievedAccount.get.id should equal (userAccount.id)
  }

  it should "find a user by email with any of" in {
    val userEmail = "user@email.com"
    val userAccount = UserAccount(UUID.randomUUID(), None, Some(userEmail))

    userByEmail.insert.value(_.email, userEmail)
    userAccountRecord.insert.value(_.id, userAccount.id).value(_.email, userAccount.email)

    val retrievedAccount = Await.result( dao.findByAnyOf(None, None, Some(userEmail)), futureTimeout)

    retrievedAccount should be (defined)
    retrievedAccount.get.id should equal (userAccount.id)
  }


  it should "retrieve a user by msisdn" in {
    val userMsisdn = "011123455"
    val userAccount = UserAccount(UUID.randomUUID(), Some(userMsisdn), None)

    userByMsisdn.insert.value(_.msisdn, userMsisdn)
    userAccountRecord.insert.value(_.id, userAccount.id).value(_.msisdn, userAccount.msisdn)

    val retrievedAccount = Await.result( dao.getByMsisdn(userMsisdn), futureTimeout)

    retrievedAccount should be (defined)
    retrievedAccount.get.id should equal (userAccount.id)
  }

  it should "find a user by msisdn with anyOf search" in {
    val userMsisdn = "011123455"
    val userAccount = UserAccount(UUID.randomUUID(), Some(userMsisdn), None)

    userByMsisdn.insert.value(_.msisdn, userMsisdn)
    userAccountRecord.insert.value(_.id, userAccount.id).value(_.msisdn, userAccount.msisdn)

    val retrievedAccount = Await.result( dao.findByAnyOf(None, Some(userMsisdn), None), futureTimeout)

    retrievedAccount should be (defined)
    retrievedAccount.get.id should equal (userAccount.id)
  }
}
