package parallelai.wallet.persistence.cassandra

import parallelai.wallet.persistence.UserAccountDAO
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
import scala.concurrent.Future
import org.joda.time.DateTime

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
      )
    );
  }

  def normaliseEmail(email : Option[String]) : Option[String] = email map { _.toLowerCase }
}

sealed class ClientApplicationRecord extends CassandraTable[ClientApplicationRecord, ClientApplication] with DBConnector {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object activationCode extends StringColumn(this)
  object active extends BooleanColumn(this)
  object accountId extends UUIDColumn(this) with PartitionKey[UUID]

  override def fromRow(row: Row): ClientApplication = {
    ClientApplication(id(row), activationCode(row), active(row), accountId(row));
  }
}

class UserAccountCassandraDAO extends UserAccountDAO  {
  val userAccountRecord = new UserAccountRecord
  val clientApplicationRecord = new ClientApplicationRecord

  implicit val session = userAccountRecord.cassandra

  override def update(userAccount: UserAccount): Unit =
    userAccountRecord
      .update
      .where( _.id eqs userAccount.id)
        .modify( _.msisdn setTo userAccount.msisdn)
        .and( _.email setTo userAccountRecord.normaliseEmail(userAccount.email) )
        .and( _.personalInfo_name setTo userAccount.personalInfo.name)
        .and( _.personalInfo_birthDate setTo userAccount.personalInfo.birthDate)
        .and( _.personalInfo_gender setTo userAccount.personalInfo.gender)
        .and( _.personalInfo_postCode setTo userAccount.personalInfo.postCode)
        .and( _.settings_maxMessagesPerWeek setTo userAccount.settings.maxMessagesPerWeek)

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

  override def getByApplicationId(applicationId: UUID): Future[Option[UserAccount]] =
    clientApplicationRecord.select.where( _.id eqs applicationId).one() flatMap { clientApplicationOption =>
      clientApplicationOption match {
        case Some(clientApplication) => getById(clientApplication.id)
        case None => Future.successful(None)
      }
    }

  override def getById(userId: UUID): Future[Option[UserAccount]] = userAccountRecord.select.where( _.id eqs userId).one()

  override def getByEmail(email: String): Future[Option[UserAccount]] = userAccountRecord.select.where( _.email eqs Some(email) ).one()

  override def getByMsisdn(msisdn: String): Future[Option[UserAccount]] = userAccountRecord.select.where( _.msisdn eqs userAccountRecord.normaliseEmail(Some(msisdn)) ).one()
}
