package parallelai.wallet.persistence

import scala.concurrent.{ExecutionContext, Promise, Future}
import java.util.concurrent.atomic.AtomicInteger
import scala.util.{Failure, Success}
import scala.concurrent.Future._
import scala.util.Failure
import scala.Some
import scala.util.Success
import ExecutionContext.Implicits.global

object QueryUtils {
  /**
   * Merges the result of the execution of the Futures[T] provided.
   * If any of the futures succeeds with Some[T] then the result is immediately returned.
   * If all are returning None[T] then None is returned
   *
   * In case of failure before any successful result is found. The failure is propagated
   *
   * @param futures
   * @tparam T
   * @return
   */
  def anyOf[T](futures: Future[Option[T]]*) : Future[Option[T]] = {
    val completionPromise = Promise[Option[T]]

    val completionCount = new AtomicInteger(futures.length)

    futures foreach {
      _.onComplete {
        _ match {
          case Success(Some(result)) => completionPromise.success(Some(result))

          case Success(None) =>
            val toComplete = completionCount.decrementAndGet()
            if(toComplete == 0)
              completionPromise.success(None)

          case Failure(exception) => completionPromise.failure(exception)
        }
      }
    }

    completionPromise.future
  }

  def findByOptional[Param, Result](param: Option[Param]) (query: Param => Future[Option[Result]]) : Future[Option[Result]] =
    param map { query } getOrElse successful(None)
  
}
