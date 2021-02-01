package parallelai.wallet.persistence.mongodb

import java.util.UUID
import org.joda.time.DateTime

import org.specs2.mutable.{Before, Specification}
import parallelai.wallet.entity.{Sale, KaredoLog}

class MongoSaleDAOSpec
  extends Specification

  with TestWithLocalMongoDb
  with Before
  with UUIDMatcher
{
  def before = clearAll()


  "SaleDAO" in {
    val sale = Sale(
      userId = UUID.randomUUID(), adId = UUID.randomUUID(), code = "XXXX")
    val id = saleDAO.insertNew(sale)
    1===1
  }
}


/*


  sequential



  "SaleDAO" should {
    "create a sale" in {

      val sale = Sale(userId = UUID.randomUUID(), adId = UUID.randomUUID(), expireTs = new DateTime, code = "XXXX", codeTs = new DateTime)
      val id = saleDAO.insertNew(sale).get
      1===1
      /*val sale2 = saleDAO.getById(id).get
      sale2.adId === sale.adId
      sale2.code === sale.code*/
    }
  }

}
*/
