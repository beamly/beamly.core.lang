package beamly.core.lang.extensions

import beamly.core.lang.TryToFuture

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

final class TryW[T](val underlying: Try[T]) extends AnyVal {
  /**
   * Converts [[scala.util.Try]] to [[scala.concurrent.Future]]
   * @return Future from Try
   */
  @inline
  def future: Future[T] = TryToFuture autoTryToFuture underlying

  /**
   * Returns successful value from underlying [[scala.util.Try]] or attempts to convert exception to value.
   * @param pf Partial function to convert exceptions to a value
   * @tparam U type of return value
   * @return Underlying value or resulting value after converting exception
   */
  @inline
  def getOrRecover[U >: T](pf: => PartialFunction[Throwable, U]): U = underlying match {
    case Success(s) => s
    case Failure(e) => pf.applyOrElse(e, throw (_: Throwable))
  }
}
