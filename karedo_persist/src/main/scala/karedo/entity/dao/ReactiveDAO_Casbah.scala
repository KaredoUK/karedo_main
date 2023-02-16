package karedo.entity.dao

/**
  * Created by charaj on 05/04/2017.
  */
abstract class ReactiveDAO_Casbah[K, T <: Keyable[K]] (implicit override val manifestT: Manifest[T], override val manifestK: Manifest[K])
  extends ParentDAO[K, T]
  with ReactiveDAO[K, T]
  with MongoConnection_Casbah
{

}

