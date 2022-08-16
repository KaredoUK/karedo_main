package karedo.entity.dao

import java.util.UUID

import com.mongodb.WriteResult

import scala.util.Try

trait DbDAO[K, T<:AnyRef] {
  def insertNew(id:K,r:T): Result[String,T]
  def getById(id:K): Result[String,T]
  def update(id:K,r:T): Result[String,T]
  def delete(id:K,r:T): Result[String,T]
  def deleteAll(): Result[String,Unit]
}




