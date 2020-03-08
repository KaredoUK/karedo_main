package parallelai.wallet.entity

import java.util.UUID
import org.joda.time.DateTime


case class RetailOffer (
  id: UUID,
  title: String,
  description: String,
  price: Double,
  imageUrl: String,
  props: Map[String, String],
  timestamp: DateTime,
  test: Option[Int]
)

case class CustomerOffer (
  id: UUID,
  title: String,
  description: String,
  value: Double,
  imageUrl: String,
  props: Map[String, String],
  timestamp: DateTime,
  test: Option[Int]
)