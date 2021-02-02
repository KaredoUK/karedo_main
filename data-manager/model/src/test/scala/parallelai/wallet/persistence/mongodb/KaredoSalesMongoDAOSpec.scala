package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.mongodb.casbah.commons.MongoDBObject
import org.specs2.mutable.Specification
import parallelai.wallet.entity.KaredoSales


class KaredoSalesMongoDAOSpec
  extends Specification
  with MongoTestUtils
  with UUIDMatcher
{
  val my = new KaredoSalesMongoDAO()
  my.dao.collection.remove(MongoDBObject())


  "MyTable" should {

    "do something" in {

      val sale = KaredoSales(userId = UUID.randomUUID(), adId = UUID.randomUUID(), code = "pippo")
      val id = my.insertNew(sale).get
      id.toString must beMatching(UUIDre)

      val read=my.findByCode("pippo").get
      read.userId should be_===(sale.userId)

    }
  }
}