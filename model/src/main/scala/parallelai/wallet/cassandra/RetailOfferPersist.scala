package parallelai.wallet.cassandra

import parallelai.wallet.entity._
import com.newzly.phantom.Implicits._
import com.datastax.driver.core.{ResultSet, Row}
import java.util.UUID

import scala.concurrent.{ Future => ScalaFuture }
import org.joda.time.DateTime
import com.datastax.driver.core.{ ResultSet, Row }
import com.newzly.phantom.iteratee.Iteratee


sealed class RetailOfferRecord private() extends CassandraTable[RetailOfferRecord, RetailOffer] {
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

object RetailOfferRecord extends RetailOfferRecord with DBConnector {
  // TODO: Set the table name through configuration
  override lazy val tableName = "retail_offer"

  def insertNewRecord(ro: RetailOffer): ScalaFuture[ResultSet] = {

    insert.value(_.id, ro.id)
      .value(_.title, ro.title)
      .value(_.description, ro.description)
      .value(_.price, ro.price)
      .value(_.imageUrl, ro.imageUrl)
      .value(_.props, ro.props)
      .value(_.timestamp, ro.timestamp)
      .value(_.test, ro.test)
      // TODO: Could use TTL here but should we?
      .ttl(4500)
      .future()
  }

  def getRetailOfferById(id: UUID): ScalaFuture[Option[RetailOffer]] = {
    select.where(_.id eqs id).one()
  }

  def getEntireTable: ScalaFuture[Seq[RetailOffer]] = {
    select.fetchEnumerator() flatMap {
      res => res run Iteratee.collect()
    }
  }
  def getRecipePage(start: Int, limit: Int): ScalaFuture[Iterator[RetailOffer]] = {
    select.fetchEnumerator() flatMap {
      res => res run Iteratee.slice(start, limit)
    }
  }

  def deleteRecipeById(id: UUID): ScalaFuture[ResultSet] = {
    delete.where(_.id eqs id).future()
  }
}
