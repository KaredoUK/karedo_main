package karedo.persist.entity.dao

/**
  * Created by charaj on 04/04/2017.
  */
trait Keyable[K] {
  def id: K
}