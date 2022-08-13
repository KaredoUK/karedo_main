package karedo.entity.dao

import java.util.UUID

import com.mongodb.WriteResult

import scala.util.Try

trait DbDAO[K, T<:AnyRef] {
  def insertNew(id:K,r:T): Try[Option[K]]
  def getById(id:K): Option[T]
  def update(id:K,r:T): Try[String]
  def delete(id:K,r:T): Try[String]
  def deleteAll()
}




