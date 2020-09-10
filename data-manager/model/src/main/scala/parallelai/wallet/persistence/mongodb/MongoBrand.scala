package parallelai.wallet.persistence.mongodb

import java.util.UUID
import org.joda.time.DateTime
import parallelai.wallet.entity._
import com.novus.salat.annotations._

object MongoBrandAds {
  def fromAd(ad: AdvertisementDetail) =
    MongoBrandAd(ad.id, ad.text, ad.publishedDate, ad.imageIds, ad.value)
}

case class MongoBrandAd
(
  @Key("_id") id: UUID=UUID.randomUUID(),
  text: String="",
  publishedDate: DateTime = new DateTime(),
  imageIds: List[String] = List[String](),
  value: Int=0)
{
  def toBrandAd(): AdvertisementDetail =
    AdvertisementDetail(id, publishedDate, text, imageIds, value)
}

case class MongoBrand
(
  @Key("_id") id: UUID,
  name: String,
  iconId: String,
  ads: List[MongoBrandAd]
  )
{

  def toMongoBrandAdList =
    List[AdvertisementDetail]()

    /*ads map {
    _.toBrandAd()
  }
  */
}
