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
import com.twitter.io.exp.VarSource.Failed
import parallelai.wallet.persistence.QueryUtils._
import com.newzly.phantom.Implicits._

sealed class UserAccountRecord extends CassandraTable[UserAccountRecord, UserAccount] with DBConnector {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object msisdn extends OptionalStringColumn(this) with SecondaryKey[Option[String]]
  object email extends OptionalStringColumn(this) with SecondaryKey[Option[String]]

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

sealed class ClientApplicationRecord extends CassandraTable[ClientApplicationRecord, ClientApplication] with DBConnector {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object activationCode extends StringColumn(this) //with Index[String]
  object active extends BooleanColumn(this)
  object accountId extends UUIDColumn(this) with SecondaryKey[UUID]

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

  implicit val session = userAccountRecord.cassandra

  override def update(userAccount: UserAccount): Unit =
    userAccountRecord
      .update
      .where(_.id eqs userAccount.id)
      .modify(_.msisdn setTo userAccount.msisdn)
      .and(_.email setTo userAccountRecord.normaliseEmail(userAccount.email))
      .and(_.personalInfo_name setTo userAccount.personalInfo.name)
      .and(_.personalInfo_birthDate setTo userAccount.personalInfo.birthDate)
      .and(_.personalInfo_gender setTo userAccount.personalInfo.gender)
      .and(_.personalInfo_postCode setTo userAccount.personalInfo.postCode)
      .and(_.settings_maxMessagesPerWeek setTo userAccount.settings.maxMessagesPerWeek)
      .and(_.active setTo userAccount.active)

  override def setActive(userId: UUID): Unit =
    userAccountRecord
      .update
      .where(_.id eqs userId)
      .modify(_.active setTo true)

  override def insertNew(userAccount: UserAccount): Unit =
    userAccountRecord.insert
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
            val userAccountFuture = getById(clientApplication.id)

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

  override def getByEmail(email: String, mustBeActive: Boolean = false): Future[Option[UserAccount]] = {
    val baseQuery = userAccountRecord
      .select
      .where(_.email eqs Some(email))

    val query = if (mustBeActive) baseQuery.and(_.active eqs true) else baseQuery

    query.one()
  }

  override def getByMsisdn(msisdn: String, mustBeActive: Boolean = false): Future[Option[UserAccount]] = {
    val baseQuery = userAccountRecord
      .select
      .where(_.msisdn eqs userAccountRecord.normaliseEmail(Some(msisdn)))

    val query = if (mustBeActive) baseQuery.and(_.active eqs true) else baseQuery

    query.one()
  }
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

  override def update(clientApp: ClientApplication): Unit =
    clientApplicationRecord
      .update
      .where(_.id eqs clientApp.id)
      .modify(_.activationCode setTo clientApp.activationCode)
      .and(_.active setTo clientApp.active)

  override def insertNew(clientApp: ClientApplication): Unit =
    clientApplicationRecord
      .insert
      .value(_.id, clientApp.id)
      .value(_.accountId, clientApp.accountId)
      .value(_.activationCode, clientApp.activationCode)
      .value(_.active, clientApp.active)
}