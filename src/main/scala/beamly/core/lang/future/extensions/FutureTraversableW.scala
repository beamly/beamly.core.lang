package beamly.core.lang.future.extensions

import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

final class FutureTraversableW[+A](val underlying: Future[Traversable[A]]) extends AnyVal {
  /**
   * Maps values in collection, creating new future based on the mapped results
   * @param f Function to map values in collection
   * @param cbf [[scala.collection.generic.CanBuildFrom]] to convert to new type of collection
   * @param executor The execution context
   * @tparam B The return type of the collection elements
   * @tparam CB The collection type returned
   * @return New future with mapped collection values
   */
  @inline
  def mapTraversable[B,CB](f: A => B)(implicit cbf: CanBuildFrom[Traversable[A], B, CB], executor: ExecutionContext): Future[CB] = {
    underlying.map(_ map f)
  }
}
