package parallelai.wallet.entity.api.offermanager

import java.util.UUID
import org.joda.time.DateTime

case class Retailer (
  id: UUID,
  name: String,
  address: String,
  account: BankAccount
)

case class BankAccount (
  sortCode: String,
  account: String,
  name: String
)

case class RetailOffer (
  id: UUID,
  title: String,
  description: String,
  price: Double,
  imageUrl: String,
  retailer: Retailer,
  props: Map[String, String],
  timestamp: DateTime,
  test: Option[Int]
)

case class CustomerOffer (
  id: UUID,
  customerId: UUID,
  retailOfferId: UUID,
  title: String,
  value: Double,
  props: Map[String, String],
  timestamp: DateTime,
  test: Option[Int]
)