package parallelai.wallet.persistence.mongodb

import java.io.ByteArrayInputStream

import org.specs2.mutable.Specification
import parallelai.wallet.entity.{MediaContentDescriptor, MediaContent}

import scala.util.Random


class MongoMediaDAOSpec
  extends Specification
  with TestWithLocalMongoDb
{
  sequential

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


    "find by name" in {
      val (mediaContent, fileContent) = sampleMediaContent

      val id = mediaDao.createNew(mediaContent)

      val retrievedOp = mediaDao.findByName(mediaContent.descriptor.name)

      retrievedOp should not be (None)

      retrievedOp.get.descriptor shouldEqual mediaContent.descriptor.copy(id = id)

      val readBuf = Array.ofDim[Byte](fileContent.length + 1)
      val readLen = retrievedOp.get.inputStream.read(readBuf)
      retrievedOp.get.inputStream.close()

      readBuf.take(readLen) shouldEqual fileContent
    }

    "delete a file" in {
      val (mediaContent, _) = sampleMediaContent

      val id = mediaDao.createNew(mediaContent)

      mediaDao.delete(id)

      mediaDao.findById(id) shouldEqual None
    }
  }
}
