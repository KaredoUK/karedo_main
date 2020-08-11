package parallelai.wallet.persistence.mongodb

import java.util.UUID

import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.commons.ValidBSONType.DBObject
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import parallelai.wallet.entity.{AdvertisementMetadata, Brand}
import com.escalatesoft.subcut.inject.NewBindingModule
import NewBindingModule._

/**
 * Created by pakkio on 29/09/2014.
 */
class MongoBrandDAOSpec extends Specification with EmbedConnection with CleanAfterExample with NoTimeConversions with MongoTestUtils  {

  sequential

  "BrandMongoDAO" should {

    implicit val bindingModule = newBindingModuleWithConfig(
      Map(
        "mongo.server.host" -> "localhost",
        "mongo.server.port" -> "27017",
        "mongo.db.name" -> "wallet_data",
        "mongo.db.user" -> "",
        "mongo.db.pwd" -> ""
      )
    )
//    implicit val bindingModule = newBindingModuleWithConfig(
//      Map(
//        "mongo.server.host" -> "localhost",
//        "mongo.server.port" -> s"$embedConnectionPort",
//        "mongo.db.name" -> "test",
//        "mongo.db.user" -> "",
//        "mongo.db.pwd" -> ""
//      )
//    )

    val brandDAO = new MongoBrandDAO


    def mybrand=Brand(name = "brand X", iconPath="iconpath",ads=List[AdvertisementMetadata]() )

    "create and retrieve a brand with a generated id " in {


      val insert = fromFuture {
        brandDAO.insertNew(mybrand)
      }
      val findAfterInsert = fromFuture(brandDAO.getById(insert.id)).get

      findAfterInsert shouldEqual insert

      println("Passed 1")

      true

    }

    "can delete one instance" in {

      val insert = fromFuture {
        brandDAO.insertNew(mybrand)
      }

      fromFuture(brandDAO.delete(insert.id))


      val findAfterDelete = fromFuture(brandDAO.getById(insert.id))




      findAfterDelete == None

    }

    "can delete all instances" in {

      val insert = fromFuture {
        brandDAO.insertNew(mybrand)
      }
     // Thread.sleep(500)

      val list=fromFuture(brandDAO.list)

      list.size shouldEqual( 2 )

      list.map( brand => fromFuture(brandDAO.delete(brand.id)))

      val list2=fromFuture(brandDAO.list)

      list.size shouldEqual( 0 )



    }

  }

}
