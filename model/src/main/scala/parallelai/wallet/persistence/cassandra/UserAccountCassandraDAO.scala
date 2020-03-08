package parallelai.wallet.persistence.cassandra

import parallelai.wallet.persistence.UserAccountDAO
import com.newzly.phantom.CassandraTable
import parallelai.wallet.entity.{UserAccount, RetailOffer}
import com.newzly.phantom.column.{OptionalPrimitiveColumn, DateTimeColumn, MapColumn, PrimitiveColumn}
import com.newzly.phantom.Implicits._
import java.util.UUID
import com.datastax.driver.core.Row
import parallelai.wallet.cassandra.RetailOfferRecord


/*
id: UUID,
                        registeredApplications: Seq[ClientApplication],
                        personalInfo: UserInfo,
                        settings: AccountSettings
 */
sealed class UserAccountRecord private() extends CassandraTable[UserAccountRecord, UserAccount] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object title extends StringColumn(this) with SecondaryKey[String]
  object description extends StringColumn(this)
  object price extends DoubleColumn(this)
  object imageUrl extends StringColumn(this)
  object props extends MapColumn[RetailOfferRecord, RetailOffer, String, String](this)
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[_]
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): RetailOffer = {
    RetailOffer(id(row), title(row), description(row), price(row), imageUrl(row), props(row), timestamp(row), test(row));
  }
}

class UserAccountCassandraDAO extends UserAccountDAO {

}
