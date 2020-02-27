package parallelai.wallet.cassandra

import parallelai.wallet.entity._
import com.newzly.phantom.Implicits._
import com.datastax.driver.core.Row
import java.util.UUID

sealed class RetailOfferRecord private() extends CassandraTable[RetailOfferRecord, RetailOffer] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder with Ascending
  object title extends StringColumn(this)
  object description extends StringColumn(this)
  object price extends DoubleColumn(this)
  object imageUrl extends StringColumn(this)
  object props extends MapColumn[RetailOfferRecord, RetailOffer, String, String](this)
  object test extends OptionalIntColumn(this)
}
