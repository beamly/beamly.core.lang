package beamly.core.lang.future.extensions

import scala.concurrent.{ExecutionContext, Future}

final class FutureFutureW[+A](val underlying: Future[Future[A]]) extends AnyVal {
  /**
   * @param executor The execution context
   * @return Flattened [[scala.concurrent.Future]]
   */
  @inline
  def join(implicit executor: ExecutionContext): Future[A] = underlying flatMap identity

  /**
   * @param executor The execution context
   * @return Flattened [[scala.concurrent.Future]]
   */
  @inline
  def flatten(implicit executor: ExecutionContext): Future[A] = underlying flatMap identity
}
