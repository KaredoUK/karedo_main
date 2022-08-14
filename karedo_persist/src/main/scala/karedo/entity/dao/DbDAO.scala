package karedo.entity.dao

import java.util.UUID

import com.mongodb.WriteResult

import scala.util.Try

trait DbDAO[K, T<:AnyRef] {
  def insertNew(id:K,r:T): Either[String,Unit]
  def getById(id:K): Either[String,T]
  def update(id:K,r:T): Either[String,Unit]
  def delete(id:K,r:T): Either[String,Unit]
  def deleteAll(): Either[String,Unit]
}




