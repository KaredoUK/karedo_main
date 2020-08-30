package parallelai.wallet.persistence.mongodb

import com.escalatesoft.subcut.inject.{Injectable, BindingModule}
import com.github.athieriot.CleanAfterExample
import com.mongodb.casbah.gridfs.GridFS

trait CleanGridFsAfterExample extends CleanAfterExample {
  this : com.github.athieriot.CleanAfterExample with com.github.athieriot.EmbedConnection =>

  def bindingModule: BindingModule

  class CleanGridFS(bindings: BindingModule) extends MongoConnection with Injectable {
    implicit val bindingModule: BindingModule = bindings

    lazy val gridfs = GridFS(db)

    def clean() = {
      gridfs.toList.map { _._id.get } foreach { gridfs.remove }
    }
  }

  val cleanGridFs = new CleanGridFS(bindingModule)

  // Looks like the CleanAfterExample is not working properly with GridFS
  override def after(): Any = {
    super.after()

    cleanGridFs.clean()
  }
}
