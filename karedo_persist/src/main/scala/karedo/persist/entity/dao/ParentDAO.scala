package karedo.persist.entity.dao

import org.slf4j.LoggerFactory
import salat._
import salat.dao.SalatDAO
import salat.global._

/**
  * Created by charaj on 05/04/2017.
  */
abstract class ParentDAO [K, T <: Keyable[K]] (implicit val manifestT: Manifest[T], val manifestK: Manifest[K])
  extends MongoConnection_Casbah
{
  val thisClass = manifestT.runtimeClass
  val simpleName = thisClass.getSimpleName
  val logger = LoggerFactory.getLogger(thisClass)

  lazy val dao = new SalatDAO[T, K](collection = db(s"${DbDAOParams.tablePrefix}$simpleName")) {}
}
