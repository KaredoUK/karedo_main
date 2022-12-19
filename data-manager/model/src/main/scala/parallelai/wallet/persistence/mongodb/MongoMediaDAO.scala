package parallelai.wallet.persistence.mongodb


import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.mongodb.casbah.commons.MongoDBObject
import com.novus.salat.dao.SalatDAO
import parallelai.wallet.entity.{Brand, MediaContent, MediaContentDescriptor}
import parallelai.wallet.persistence.MediaDAO
import java.io.FileInputStream
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import scala.util.Try
import scala.util.Failure
import scala.util.Success

class MongoMediaDAO(implicit val bindingModule: BindingModule) extends MediaDAO with MongoConnection with Injectable {

  lazy val gridfs = GridFS(db)

  override def createNew(newContent: MediaContent): String = {
    try {

      db.getCollection("fs.chunks").createIndex(MongoDBObject("files_id" -> 1, "n"->1),MongoDBObject("unique" ->  1));
      val newIdOp = gridfs(newContent.inputStream) { writeOp =>
        writeOp.filename = newContent.descriptor.name
        writeOp.contentType = newContent.descriptor.contentType
      }

      newIdOp.get.asInstanceOf[ObjectId].toHexString
    } catch {
      case e:Exception => e.getMessage
    }
  }

  private def stringAsObjectId(idStr: String) =  new ObjectId(idStr)
    
  override def findById(id: String): Option[MediaContent] =
    {
      // tries to get object id from id and if fails go in the second lef of match
      Try(stringAsObjectId(id)).map { 
        objId => gridfs.findOne(objId).flatMap {
          dbfs => Some(extractContent(dbfs))
        }
      }
      
      // check if we succeeded and return last Option
      match {
        case Success(x) => x
        case _ => None
      }
    }

  override def findByName(name: String): Option[MediaContent] =
    gridfs.findOne(name) map { extractContent }

  override def delete(id: String): Unit =
    // if stringsucceeded then remove
    Try(stringAsObjectId(id))
      .map(gridfs.remove)
     

  override def list: List[MediaContentDescriptor] =
    gridfs.toList.map { extractDescriptor }

  private def extractDescriptor(dbFile: GridFSDBFile): MediaContentDescriptor =
    MediaContentDescriptor(name = dbFile.filename.get, contentType = dbFile.contentType.get, id = dbFile._id.get.toHexString)

  private def extractContent(dbFile: GridFSDBFile): MediaContent =
    MediaContent( extractDescriptor(dbFile), dbFile.inputStream )

}
