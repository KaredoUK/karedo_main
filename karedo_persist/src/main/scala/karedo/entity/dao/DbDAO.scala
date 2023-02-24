package karedo.entity.dao

import karedo.util.Result

import scala.concurrent.Future
import scala.util.Try

trait DbDAO[K, T <: Keyable[K]] {
  type F_UNIT = Future[Unit]
  type F_TYPE = Future[T]
  type F_ID = Future[K]
  type F_L_TYPE = Future[List[T]]
  type F_L_ID = Future[List[K]]

  type R_UNIT = Result[String,Unit]
  type R_TYPE = Result[String,T]
  type R_L_TYPE = Result[String, List[T]]
  type R_L_ID = Result[String, List[K]]

  @deprecated("use insertNew_f instead", "2017-04-12")
  def insertNew(r:T): R_TYPE

  @deprecated("use find_f instead", "2017-04-12")
  def find(id:K): R_TYPE

  @deprecated("use findAll_f instead", "2017-04-12")
  def findAll(): R_L_TYPE

  @deprecated("use ids_f instead", "2017-04-12")
  def ids: R_L_ID

  @deprecated("use update_f instead", "2017-04-12")
  def update(r:T): R_TYPE

  @deprecated("use delete_f instead", "2017-04-12")
  def delete(r:T): R_TYPE

  @deprecated("use deleteAll_f instead", "2017-04-12")
  def deleteAll(): R_UNIT

  @deprecated("use lock_f instead", "2017-04-12")
  def lock(id: K, lockId: String, retries: Int):  R_TYPE

  @deprecated("use unlock_f instead", "2017-04-12")
  def unlock(id: K, lockId: String): R_TYPE

  @deprecated("use findByField_f instead", "2017-04-12")
  def findByAccount(value: String, field: String): R_L_TYPE

  def findByField_f(value: String, field: String): F_L_TYPE
  def insert_f(r: T): F_ID
  def update_f(r: T): F_UNIT
  def upsert_f(r: T): F_UNIT
  def find_f(id: K): F_TYPE
  def findAll_f(): F_L_TYPE
  def ids_f: F_L_ID
  def delete_f(r: T): F_UNIT
  def deleteAll_f(): F_UNIT
  def lock_f(id: K, lockId: String, retries: Int):  F_TYPE
  def unlock_f(id: K, lockId: String): F_TYPE
}

//trait DbDAOExtensions[K, T <: Keyable[K]] {
//  type RESULT_LIST = Result[String, List[T]]
//  type F_L_TYPE = Future[List[T]]
//
//}

object DbDAOParams {
  var tablePrefix = "KAR_"
}


