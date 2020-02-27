package parallelai.wallet.entity


case class RetailOffer (
  id: Int,
  title: String,
  description: String,
  price: Double,
  imageUrl: String,
  props: Map[String, String],
  timestamp: Int,
  test: Option[Int]
)

