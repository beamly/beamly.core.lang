package beamly.core.lang.future.extensions

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

final class FutureTryW[+A](val underlying: Future[Try[A]]) extends AnyVal {
  /**
   * Flattens to future.
   * @param executor The execution context
   * @return Future completed with [[scala.util.Try]] value
   */
  @inline
  def join(implicit executor: ExecutionContext): Future[A] = underlying flatMap {
    case Success(s) => Future successful s
    case Failure(e) => Future failed e
  }
}
