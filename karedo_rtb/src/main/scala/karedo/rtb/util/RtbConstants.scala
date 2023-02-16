package karedo.rtb.util

import akka.http.scaladsl.model.{HttpHeader, headers}

import scala.concurrent.duration._
import scala.collection.immutable._
import scala.collection.mutable

/**
  * Created by crajah on 04/12/2016.
  */
trait RtbConstants {
  val bid_tmax = 250
  val bid_bcat = List("IAB25", "IAB26")

  val banner_w = 300
  val banner_h = 250
  val banner_pos = 1
  val banner_btype = List(1, 3, 4)

  val app_included = true
  val app_id = "karedo"
  val app_name = "Karedo"
  val app_bundle = "uk.co.karedo"
  val app_domain = "karedo.co.uk"
  val app_storeurl_ios = "https://itunes.apple.com/WebObjects/MZStore.woa/wa/viewSoftware?id=1195450203&mt=8"
  val app_storeurl_android = "tbc"
  val app_privacypolicy = 1
  val app_paid = 0
  val secure_ad = 0

  val site_included = false
  val site_id = "karedo"
  val site_name = "Karedo"
  val site_domain = "karedo.co.uk"
  val site_privacypolicy = 1
  val site_page = "http://karedo.co.uk/main"

  val floor_price = 0.50

  val rtb_max_wait = (bid_tmax * 20) milliseconds
  val dispatcher_max_wait = (bid_tmax * 100) milliseconds

  implicit def mapToMutableMap[K, V](fromMap: Map[K, V]):mutable.Map[K, V] = {
    val toMap: mutable.Map[K, V] = mutable.Map()
    fromMap.map(toMap += _)
    toMap
  }

  implicit def mutableMapToMap[K, V](fromMap: mutable.Map[K, V]): Map[K,V] = fromMap.toMap

  def addIPAddressToXFFHeader(mutableHeaderMap: mutable.Map[String, String], ipOption: Option[String]): mutable.Map[String, String] = {
    addCommaSeparatedValueToEndOfHeader("X-Forwarded-For", mutableHeaderMap, ipOption)
  }

  def addCommaSeparatedValueToEndOfHeader(header: String, mutableHeaderMap: mutable.Map[String, String], ipOption: Option[String]): mutable.Map[String, String] = {
    val updatedHeaderMap:mutable.Map[String, String] = mutableHeaderMap.map { h =>
      (h._1,
        h._1 match {
          case s if s == header => h._2 + ", " + ipOption.get
          case _ => h._2
        }
      )
    }

    if( ! updatedHeaderMap.contains(header) && ipOption.isDefined ) {
      updatedHeaderMap(header) = ipOption.get
    }

    updatedHeaderMap
  }

  def makeHttpHeaderFromMap(fromMap: Map[String, String]): List[HttpHeader] = {
    val http_headers:mutable.MutableList[HttpHeader] = mutable.MutableList()

    fromMap.map { h =>
      http_headers += headers.RawHeader(h._1, h._2)
    }

    http_headers.toList
  }

  def addHeaderToMap(fromMap: Map[String, String], name: String, value: String): Map[String, String] = {
    fromMap + (name -> value)
  }

  def addOptionHeaderToMap(fromMap: Map[String, String], name: String, value: Option[String]): Map[String, String] = {
    value match {
      case Some(x) => addHeaderToMap(fromMap, name, x)
      case None => fromMap
    }
  }

  def addHeaderToMutableMap(fromMap: mutable.Map[String, String], name: String, value: String) = {
    fromMap += (name -> value)
  }
}
