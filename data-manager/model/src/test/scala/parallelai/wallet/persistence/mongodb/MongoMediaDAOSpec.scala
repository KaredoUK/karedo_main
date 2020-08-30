package parallelai.wallet.persistence.mongodb

import java.io.ByteArrayInputStream

import com.github.athieriot.{CleanAfterExample, EmbedConnection}
import com.mongodb.casbah.gridfs.Imports._
import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import com.escalatesoft.subcut.inject.{Injectable, BindingModule, NewBindingModule}
import NewBindingModule._
import parallelai.wallet.entity.{MediaContentDescriptor, MediaContent}
import parallelai.wallet.persistence.MediaDAO

import scala.util.Random


class MongoMediaDAOSpec extends Specification with EmbedConnection with CleanGridFsAfterExample with NoTimeConversions with MongoTestUtils {
  sequential

  implicit lazy val bindingModule = newBindingModuleWithConfig(
    Map(
      "mongo.server.host" -> "localhost",
      "mongo.server.port" -> s"$embedConnectionPort",
      "mongo.db.name" -> "test",
      "mongo.db.user" -> "",
      "mongo.db.pwd" -> ""
    )
  )

  val mediaDao = new MongoMediaDAO

  def sampleMediaContent: (MediaContent, Array[Byte]) = {
    val descriptor = MediaContentDescriptor("fileName" + Random.nextInt(1000), "application/jpeg")
    val fileContent: Array[Byte] = (1 to 200000) map { _.toByte } toArray

    val inputStream = new ByteArrayInputStream(fileContent)

    (MediaContent( descriptor, inputStream ), fileContent)
  }

  "MongoMediaDAO " should {

    "Create a new Media content and allow to retrieve it" in {
      val (mediaContent, _) = sampleMediaContent

      val id = mediaDao.createNew(mediaContent)

      mediaDao.findById(id) should not be (None)
    }

    "Retrieve the same content of the new created file" in {
      val (mediaContent, fileContent) = sampleMediaContent

      val id = mediaDao.createNew(mediaContent)

      val retrieved = mediaDao.findById(id).get

      retrieved.descriptor shouldEqual mediaContent.descriptor.copy(id = id)

      val readBuf = Array.ofDim[Byte](fileContent.length + 1)
      val readLen = retrieved.inputStream.read(readBuf)
      retrieved.inputStream.close()

      readBuf.take(readLen) shouldEqual fileContent
    }

    // Having problems with more than two tests, any test placed after the second fails with error:
    // no md5 returned from server: { "serverUsed" : "localhost:12345" , "errmsg" : "exception: Can't get runner for query { files_id: ObjectId('5443ffbb03642684dd6506d8'), n: { $gte: 0 } }" , "code" : 17241 , "ok" : 0.0}
    //com.mongodb.MongoException: no md5 returned from server: { "serverUsed" : "localhost:12345" , "errmsg" : "exception: Can't get runner for query { files_id: ObjectId('5443ffbb03642684dd6506d8'), n: { $gte: 0 } }" , "code" : 17241 , "ok" : 0.0}

//    "find by name" in {
//      val (mediaContent, fileContent) = sampleMediaContent
//
//      val id = mediaDao.createNew(mediaContent)
//
//      val retrievedOp = mediaDao.findByName(mediaContent.descriptor.name)
//
//      retrievedOp should not be (None)
//
//      retrievedOp.get.descriptor shouldEqual mediaContent.descriptor.copy(id = id)
//
//      val readBuf = Array.ofDim[Byte](fileContent.length + 1)
//      val readLen = retrievedOp.get.inputStream.read(readBuf)
//      retrievedOp.get.inputStream.close()
//
//      readBuf.take(readLen) shouldEqual fileContent
//    }
//
//    "delete a file" in {
//      val (mediaContent, _) = sampleMediaContent
//
//      val id = mediaDao.createNew(mediaContent)
//
//      mediaDao.delete(id)
//
//      mediaDao.findById(id) shouldEqual None
//    }
  }
}
