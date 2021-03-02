package parallelai.wallet.persistence.mongodb

import java.util.UUID
import org.joda.time.DateTime
import parallelai.wallet.entity.KaredoTypes.KaredoPoints
import parallelai.wallet.entity._
import com.novus.salat.annotations._
import parallelai.wallet.persistence.Interaction

object MongoBrandAds {
  def fromAd(ad: AdvertisementDetail) =
    MongoBrandAd(
      ad.id,
      ad.shortText,
      ad.detailedText,
      ad.termsAndConditions,
      ad.summaryImages,
      ad.publishedDate,
      ad.startDate,
      ad.endDate,
      ad.detailImages,
      ad.karedos)
}

case class MongoBrandAd
(
  @Key("_id") id: UUID=UUID.randomUUID(),
  shortText: String="",
  detailedText: String="",
  termsAndConditions: String="",
  summaryImages: List[SummaryImageDB],
  publishedDate: DateTime = new DateTime(),
  startDate: DateTime,
  endDate: DateTime,
  detailImages: List[String] = List[String](),
  karedos: KaredoPoints=0)
{
  def toBrandAd(): AdvertisementDetail =
    AdvertisementDetail(
      id,
      shortText,
      detailedText,
      termsAndConditions,
      summaryImages,
      publishedDate,
      startDate,
      endDate,
      detailImages,
      karedos)
}

case class MongoBrand
(
  @Key("_id") id: UUID,
  name: String,
  iconId: String,
  ads: List[MongoBrandAd],
  interactions: List[Interaction]
  )
{

  def toMongoBrandAdList =
    List[AdvertisementDetail]()

    /*ads map {
    _.toBrandAd()
  }
  */
}
