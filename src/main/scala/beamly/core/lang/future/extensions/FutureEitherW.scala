package beamly.core.lang.future.extensions

import scala.concurrent.{ExecutionContext, Future}

final class FutureEitherW[+A](val underlying: Future[Either[Throwable, A]]) extends AnyVal {
  /**
   * Converts to successful or failed future
   * @param executor The execution context
   * @return Future, mapping left to failed Future and right to successful Future
   */
  @inline
  def join(implicit executor: ExecutionContext): Future[A] = underlying flatMap (_.fold(Future.failed, Future.successful))
}
