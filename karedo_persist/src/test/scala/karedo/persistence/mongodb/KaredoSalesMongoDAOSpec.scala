package karedo.persistence.mongodb

import java.util.UUID

@RunWith(classOf[JUnitRunner])
class KaredoSalesMongoDAOSpec
  extends Specification
  with MongoTestUtils
  with UUIDMatcher
{
  val my = new KaredoSalesMongoDAO()
  my.dao.collection.remove(MongoDBObject())


  "MyTable" should {

    "do something" in {

      val sale:KaredoSales = KaredoSales(accountId = UUID.randomUUID(), points=500,saleType="OFFER",
          adId = Some(UUID.randomUUID()), code = Some("pippo"))
      val id = my.insertNew(sale).get
      id.toString must beMatching(UUIDre)

      val read=my.findByCode("pippo").get
      read.accountId should be_===(sale.accountId)

    }
  }
}