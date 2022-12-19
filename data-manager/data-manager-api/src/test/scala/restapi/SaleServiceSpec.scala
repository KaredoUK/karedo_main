package restapi

import java.awt.image.ImageConsumer
import java.util.UUID

import com.parallelai.wallet.datamanager.data._
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.mockito.Matchers.{eq => argEq}
import org.specs2.runner.JUnitRunner
import parallelai.wallet.entity._
import spray.client.pipelining._
import util.ApiHttpClientSpec

import scala.concurrent.duration._
import scala.util.Random


@RunWith(classOf[JUnitRunner])
class SaleServiceSpec
  extends ApiHttpClientSpec {

  import com.parallelai.wallet.datamanager.data.ApiDataJsonProtocol._
  import parallelai.wallet.util.SprayJsonSupport._

  sequential

  override def responseTimeout = 30.seconds

  val UUIDre = """^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$""".r

  "Sale Service" should {
    "PARALLELAI-116: create a sale" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[SaleResponse]

      val merchant = UserAccount(UUID.randomUUID(), userType = "MERCHANT", msisdn = Some("1234"), email = Some("email"))

      val sale = KaredoSales(saleType = "SALE", accountId = userId, points = 50)
      mockedSaleDAO.insertNew(any[KaredoSales]) returns Some(sale.id)

      val request = SaleCreate(merchant.id, 100)
      val response = wait(pipeline {
        Post(s"$serviceUrl/sale/" + userId.toString + "/create", request).withHeaders(headers)
      })

      response.saleId.toString must beMatching(UUIDre)

    }

    "PARALLELAI-117: get sale detail" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[SaleDetail]
      val merchantname = "Merchant xxx"

      val merchant = UserAccount(UUID.randomUUID(), userType = "MERCHANT", msisdn = Some("1234"), email = Some("email"),
        personalInfo = UserPersonalInfo(name = merchantname))

      println("Merchant : " + merchant)

      val sale = KaredoSales(saleType = "SALE", accountId = merchant.id, points = 50)

      println("Sale : " + sale)
      mockedSaleDAO.findById(sale.id) returns Some(sale)
      mockedUserAccountDAO.getById(merchant.id) returns Some(merchant)


      val response = wait(pipeline {
        Get(s"$serviceUrl/sale/" + sale.id.toString).withHeaders(headers)
      })

      response.merchantName === merchantname

    }
    "PARALLELAI-118: sale completed" in new WithMockedPersistenceRestService {
      val pipeline = sendReceive ~> unmarshal[SaleResponse]
      val merchantname = "Merchant xxx"

      val merchant = UserAccount(UUID.randomUUID(), userType = "MERCHANT", msisdn = Some("1234"), email = Some("email"),
        personalInfo = UserPersonalInfo(name = merchantname))

      println("Merchant : " + merchant)

      val sale = KaredoSales(saleType = "SALE", accountId = merchant.id, points = 50)

      println("Sale : " + sale)
      mockedSaleDAO.findById(sale.id) returns Some(sale)
      mockedSaleDAO.complete(sale.id) returns Some(sale.copy(dateConsumed = Some(new DateTime)))
      mockedUserAccountDAO.getById(merchant.id) returns Some(merchant)

      val request = SaleComplete(merchant.id, sale.id)

      val response = wait(pipeline {
        Post(s"$serviceUrl/sale/complete", request).withHeaders(headers)
      })

      response.saleId === sale.id

    }
    "PARALLELAI-119/a: set and get change" in new WithMockedPersistenceRestService {

      val p1 = sendReceive ~> unmarshal[KaredoChange]
      val change = Random.nextDouble() * 1000 + 1000
      mockedChangeDAO.findByCurrency(any[String]) returns None
      mockedChangeDAO.insertNew(any[KaredoChangeDB]) returns Some(UUID.randomUUID())
      val r1 = wait(p1 {
        Post(s"$serviceUrl/merchant/karedos/GBP", KaredoChange("GBP", change)).withHeaders(headers)
      })

      mockedChangeDAO.findByCurrency(any[String]) returns Some(KaredoChangeDB(currency="GBP",change=change))
      val pipeline = sendReceive ~> unmarshal[KaredoChange]
      val response = wait(pipeline {
        Get(s"$serviceUrl/merchant/karedos/GBP").withHeaders(headers)
      })
      response.change must beCloseTo(change, 1)
    }
  }

}
