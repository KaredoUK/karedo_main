package karedo.entity.dao

import org.joda.time.{DateTime, DateTimeZone}

object DbDao {
  def now = new DateTime(DateTimeZone.UTC)
}

trait DbDAO[K, T<:AnyRef] {

  def insertNew(r:T): Result[String,T]
  def insertNew(id:K,r:T): Result[String,T]
  def getById(id:K): Result[String,T]
  def update(id:K,r:T): Result[String,T]
  def delete(id:K,r:T): Result[String,T]
  def deleteAll(): Result[String,Unit]
}




