package karedo.entity.dao

import karedo.util.Result

trait DbDAO[K, T <: Keyable[K]] {

  def insertNew(r:T): Result[String,T]
  def find(id:K): Result[String,T]
  def ids: Result[String, List[K]]
  def update(r:T): Result[String,T]
  def delete(r:T): Result[String,T]
  def deleteAll(): Result[String,Unit]
  def lock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts", max: Int):  Result[String,T]
  def unlock(id: K, transId: String, transField: String = "lockedBy", tsField: String = "ts"): Result[String,T]

  }




