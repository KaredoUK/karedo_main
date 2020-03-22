package parallelai.wallet.persistence.cassandra

import parallelai.wallet.persistence.{ClientApplicationDAO, UserAccountDAO}
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import parallelai.wallet.entity._
import com.newzly.phantom.column.{OptionalPrimitiveColumn, DateTimeColumn, MapColumn, PrimitiveColumn}
import com.newzly.phantom.Implicits._
import java.util.UUID
import com.datastax.driver.core.Row
import parallelai.wallet.cassandra.{DBConnector, RetailOfferRecord}
import parallelai.wallet.entity.UserAccount
import parallelai.wallet.entity.ClientApplication
import parallelai.wallet.entity.RetailOffer
import parallelai.wallet.entity.UserInfo
import scala.concurrent.{Promise, Future}
import scala.concurrent.Future._
import org.joda.time.DateTime
import scala.async.Async.{async, await}
import java.util.concurrent.atomic.AtomicInteger
import scala.util.{Failure, Success, Try}
import parallelai.wallet.persistence.QueryUtils._
import parallelai.wallet.entity.AccountSettings
import parallelai.wallet.entity.ClientApplication
import scala.Some
import parallelai.wallet.entity.UserAccount
import parallelai.wallet.entity.UserInfo

sealed class UserAccountRecord extends CassandraTable[UserAccountRecord, UserAccount] with DBConnector {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object msisdn extends OptionalStringColumn(this) 
  object email extends OptionalStringColumn(this) 

  //PersonalInfo
  object personalInfo_name extends StringColumn(this)
  object personalInfo_postCode extends OptionalStringColumn(this)
  object personalInfo_birthDate extends OptionalDateTimeColumn(this)
  object personalInfo_gender extends OptionalStringColumn(this)

  //Settings
  object settings_maxMessagesPerWeek extends IntColumn(this)

  object active extends BooleanColumn(this)

  // Need to add columns otherwise the table creation won't work
  addColumn(id)
  addColumn(msisdn)
  addColumn(email)
  addColumn(personalInfo_name)
  addColumn(personalInfo_postCode)
  addColumn(personalInfo_birthDate)
  addColumn(personalInfo_gender)
  addColumn(settings_maxMessagesPerWeek)
  addColumn(active)

  override def fromRow(row: Row): UserAccount = {
    UserAccount(
      id(row),
      msisdn(row),
      email(row),
      UserInfo(
        personalInfo_name(row),
        personalInfo_postCode(row),
        personalInfo_birthDate(row),
        personalInfo_gender(row)
      ),
      AccountSettings(
        settings_maxMessagesPerWeek(row)
      ),
      active(row)
    );
  }

  def normaliseEmail(email: Option[String]): Option[String] = email map {
    _.toLowerCase
  }
}

sealed class EmailUserLookupRecord extends CassandraTable[EmailUserLookupRecord, EmailUserLookup] with DBConnector {
  object email extends StringColumn(this) with PartitionKey[String]
  object userId extends UUIDColumn(this) with Index[UUID]

  addColumn(email)
  addColumn(userId)

  override def fromRow(row: Row): EmailUserLookup = EmailUserLookup(email(row), userId(row))
}

sealed class MsisdnUserLookupRecord extends CassandraTable[MsisdnUserLookupRecord, MsisdnUserLookup] with DBConnector {
  object msisdn extends StringColumn(this) with PartitionKey[String]
  object userId extends UUIDColumn(this) with Index[UUID]

  addColumn(msisdn)
  addColumn(userId)

  override def fromRow(row: Row): MsisdnUserLookup = MsisdnUserLookup(msisdn(row), userId(row))
}

sealed class ClientApplicationRecord extends CassandraTable[ClientApplicationRecord, ClientApplication] with DBConnector {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object activationCode extends StringColumn(this)
  object active extends BooleanColumn(this)
  object accountId extends UUIDColumn(this) with Index[UUID]

  // Need to add columns otherwise the table creation won't work
  addColumn(id)
  addColumn(activationCode)
  addColumn(active)
  addColumn(accountId)

  override def fromRow(row: Row): ClientApplication = {
    ClientApplication(id(row), accountId(row), activationCode(row), active(row));
  }
}

class UserAccountCassandraDAO extends UserAccountDAO {
  val userAccountRecord = new UserAccountRecord
  val clientApplicationRecord = new ClientApplicationRecord
  val userByEmail = new EmailUserLookupRecord
  val userByMsisdn = new MsisdnUserLookupRecord

  implicit val session = userAccountRecord.cassandra

  override def update(userAccount: UserAccount): Future[Unit] =
    userAccountRecord
      .update
      .where(_.id eqs userAccount.id)
      .modify(_.personalInfo_name setTo userAccount.personalInfo.name)
      .and(_.personalInfo_birthDate setTo userAccount.personalInfo.birthDate)
      .and(_.personalInfo_gender setTo userAccount.personalInfo.gender)
      .and(_.personalInfo_postCode setTo userAccount.personalInfo.postCode)
      .and(_.settings_maxMessagesPerWeek setTo userAccount.settings.maxMessagesPerWeek)
      .and(_.active setTo userAccount.active)
      .future()
      .map { _ =>  }

  override def setActive(userId: UUID): Future[Unit] =
    userAccountRecord
      .update
      .where(_.id eqs userId)
      .modify(_.active setTo true)
      .future()
      .map { _ =>  }


  override def insertNew(userAccount: UserAccount): Future[Unit] = {
    val accountCreationFuture = userAccountRecord.insert
      .value(_.id, userAccount.id)
      .value(_.msisdn, userAccount.msisdn)
      .value(_.email, userAccount.email)
      // User Info
      .value(_.personalInfo_name, userAccount.personalInfo.name)
      .value(_.personalInfo_birthDate, userAccount.personalInfo.birthDate)
      .value(_.personalInfo_gender, userAccount.personalInfo.gender)
      .value(_.personalInfo_postCode, userAccount.personalInfo.postCode)
      // Account Settings
      .value(_.settings_maxMessagesPerWeek, userAccount.settings.maxMessagesPerWeek)
      .future()
      .map { _ =>}

    val emailLookupFuture : Future[Unit] = userAccount.email.fold[Future[Unit]]( successful[Unit]() )  { email => userByEmail.insert.value(_.email, email).value(_.userId, userAccount.id).future().map { _ => () } }
    val msisdnLookupFuture : Future[Unit] = userAccount.msisdn.fold[Future[Unit]]( successful[Unit]() )  { msisdn => userByMsisdn.insert.value(_.msisdn, msisdn).value(_.userId, userAccount.id).future().map { _ => () } }

    sequence( List(accountCreationFuture, emailLookupFuture, msisdnLookupFuture) ) map { _ => }
  }

  override def findByAnyOf(applicationIdOpt: Option[UUID], msisdnOpt: Option[String], emailOpt: Option[String]): Future[Option[UserAccount]] = {
    val byAppId: Future[Option[UserAccount]] = findByOptional(applicationIdOpt) {  getByApplicationId(_) }
    val byMsidn: Future[Option[UserAccount]] = findByOptional(msisdnOpt) { getByMsisdn(_) }
    val byEmail: Future[Option[UserAccount]] = findByOptional(emailOpt) { getByEmail(_) }

    anyOf(byAppId, byMsidn, byEmail)
  }

  override def getByApplicationId(applicationId: UUID, mustBeActive: Boolean = false): Future[Option[UserAccount]] =
    clientApplicationRecord.select.where(_.id eqs applicationId).one() flatMap {
      clientApplicationOption =>
        clientApplicationOption match {
          case Some(clientApplication) =>
            val userAccountFuture = getById(clientApplication.accountId)

            if (mustBeActive)
              userAccountFuture map { _.filter { _.active } }
            else
              userAccountFuture

          case None => Future.successful(None)
        }
    }

  override def getById(userId: UUID): Future[Option[UserAccount]] =
    userAccountRecord
      .select
      .where(_.id eqs userId)
      .one()


  private def getByLookup(mustBeActive: Boolean) ( lookup : Future[ Option[ { def userId: UUID } ] ] ) = {
    val user : Future[Option[UserAccount]] = lookup flatMap {
      _ match {
        case Some(lookupRecord) => getById(lookupRecord.userId)
        case None => successful(None)
      }
    }

    if(mustBeActive) user map { _.filter( _.active ) }
    else user
  }

  override def getByEmail(email: String, mustBeActive: Boolean = false): Future[Option[UserAccount]] =
    getByLookup(mustBeActive) {
      userByEmail
        .select
        .where( _.email eqs email )
        .one()
    }

  override def getByMsisdn(msisdn: String, mustBeActive: Boolean = false): Future[Option[UserAccount]] =
    getByLookup(mustBeActive) {
      userByMsisdn
        .select
        .where( _.msisdn eqs msisdn )
        .one()
    }


  override def setEmail(userId: UUID, email: String) : Future[Unit] = async {
    val oldRecord = await { getById(userId) }

    oldRecord match {
      case Some(account) =>
        val oldEmailOp = account.email
        userAccountRecord.update.modify( _.email setTo Some(email)).future()
        //val deleteOtherEmail = oldEmailOp map { oldEmail => userByEmail.delete.where( _.email eqs oldEmail).execute().map( _ => () ) } getOrElse successful[Unit]( () )
        userByEmail.insert.value(_.email, email).value(_.userId, userId).future()

      case None => throw new IllegalArgumentException(s"Cannot find user with ID ${userId}")
    }
  }

  override def setMsisdn(userId: UUID, msisdn: String) : Future[Unit] =  async {
    val oldRecord = await { getById(userId) }

    oldRecord match {
      case Some(account) =>
        val oldMsidnOp = account.msisdn
        userAccountRecord.update.modify( _.msisdn setTo Some(msisdn)).execute()
        oldMsidnOp foreach { oldMsisdn => userByMsisdn.delete.where( _.msisdn eqs oldMsisdn).execute() }
        userByMsisdn.insert.value(_.msisdn, msisdn).value(_.userId, userId).execute()

      case None => throw new IllegalArgumentException(s"Cannot find user with ID ${userId}")
    }
  }

  def createTables : List[Future[Unit]] =
    List(
      userByEmail.createTable(),
      userByMsisdn.createTable(),
      userAccountRecord.createTable(),
      clientApplicationRecord.createTable()
    )
}

class ClientApplicationCassandraDAO extends ClientApplicationDAO {
  val clientApplicationRecord = new ClientApplicationRecord

  implicit val session = clientApplicationRecord.cassandra

  override def getById(applicationId: UUID): Future[Option[ClientApplication]] =
    clientApplicationRecord
      .select
      .where(_.id eqs applicationId)
      .one()

  override def findByUserId(userId: UUID): Future[Seq[ClientApplication]] =
    clientApplicationRecord
      .select
      .where(_.accountId eqs userId)
      .fetch()

  override def update(clientApp: ClientApplication): Future[Unit] =
    clientApplicationRecord
      .update
      .where(_.id eqs clientApp.id)
      .modify(_.activationCode setTo clientApp.activationCode)
      .and(_.active setTo clientApp.active)
      .future()
      .map { _ =>}

  override def insertNew(clientApp: ClientApplication): Future[Unit] =
    clientApplicationRecord
      .insert
      .value(_.id, clientApp.id)
      .value(_.accountId, clientApp.accountId)
      .value(_.activationCode, clientApp.activationCode)
      .value(_.active, clientApp.active)
      .future()
      .map { _ =>}
}