package karedo.persistence.mongodb

import java.util.UUID
import org.joda.time.DateTime
import karedo.entity.KaredoTypes.KaredoPoints
import karedo.entity._
import com.novus.salat.annotations._
import karedo.persistence.Interaction

object MongoBrandAds {
  def fromAd(ad: AdvertisementDetail) =
    MongoBrandAd(
      ad.id,
      ad.shortText,
      ad.detailedText,
      ad.termsAndConditions,
      ad.shareDetails,
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
  shareDetails: String="",
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
      shareDetails,
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
