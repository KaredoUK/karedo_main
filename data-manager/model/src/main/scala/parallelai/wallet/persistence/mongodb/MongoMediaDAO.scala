package parallelai.wallet.persistence.mongodb


import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.novus.salat.dao.SalatDAO
import parallelai.wallet.entity.{Brand, MediaContent, MediaContentDescriptor}
import parallelai.wallet.persistence.MediaDAO
import java.io.FileInputStream
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._

class MongoMediaDAO(implicit val bindingModule: BindingModule) extends MediaDAO with MongoConnection with Injectable {

  lazy val gridfs = GridFS(db)

  override def createNew(newContent: MediaContent): String = {
    val newIdOp = gridfs(newContent.inputStream) { writeOp =>
      writeOp.filename = newContent.descriptor.name
      writeOp.contentType = newContent.descriptor.contentType
    }

    newIdOp.get.asInstanceOf[ObjectId].toHexString
  }

  override def findById(id: String): Option[MediaContent] =
    gridfs.findOne( stringAsObjectId(id) ) map { extractContent }


  override def findByName(name: String): Option[MediaContent] =
    gridfs.findOne(name) map { extractContent }

  override def delete(id: String): Unit =
    gridfs.remove(stringAsObjectId(id))

  override def list: List[MediaContentDescriptor] =
    gridfs.toList.map { extractDescriptor }

  private def stringAsObjectId(idStr: String): ObjectId = new ObjectId(idStr)

  private def extractDescriptor(dbFile: GridFSDBFile): MediaContentDescriptor =
    MediaContentDescriptor(name = dbFile.filename.get, contentType = dbFile.contentType.get, id = dbFile._id.get.toHexString)

  private def extractContent(dbFile: GridFSDBFile): MediaContent =
    MediaContent( extractDescriptor(dbFile), dbFile.inputStream )

}
