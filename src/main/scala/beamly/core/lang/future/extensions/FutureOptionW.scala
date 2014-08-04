package beamly.core.lang.future
package extensions

import scala.concurrent.{Promise, ExecutionContext, Future}
import scala.util.Success

final class FutureOptionW[+A](val underlying: Future[Option[A]]) extends AnyVal {
  /**
   * Maps some value to new future option.
   * {{
   * Future.successful(Some(2)) mapOpt (_ + 1) == Future { 3 }
   * }}
   *
   * @param f Function which maps some value to new value
   * @param executor The execution context
   * @tparam B Return type
   * @return New future option with mapped some value
   */
  @inline
  def mapOpt[B](f: A => B)(implicit executor: ExecutionContext): Future[Option[B]] =
    underlying map (_ map f)

  @inline
  def flatMapOpt[B](f: A => Future[Option[B]])(implicit executor: ExecutionContext): Future[Option[B]] =
    underlying flatMap (_ map f getOrElse futureNone)

  /**
   * If this future value is none, then use the other future value.
   * @param other Function used to get another future option, if the original was a future none
   * @param executor The execution context
   * @tparam B The new return type
   * @return A future option based on the original or other future calculation
   */
  @inline
  def orElse[B >: A](other: => Future[Option[B]])(implicit executor: ExecutionContext): Future[Option[B]] = {
    val promise = Promise[Option[B]]()
    underlying onComplete {
      case Success(None) =>
        promise completeWith other
      case result =>
        promise complete result
    }
    promise.future
  }
}
