package karedo.entity.dao

trait DbDAO[K, T <: Keyable[K]] {

  def insertNew(r:T): Result[String,T]
  def find(id:K): Result[String,T]
  def update(r:T): Result[String,T]
  def delete(r:T): Result[String,T]
  def deleteAll(): Result[String,Unit]
}




