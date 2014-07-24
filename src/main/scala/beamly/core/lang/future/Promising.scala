package beamly.core.lang.future

import scala.concurrent.{Future, Promise}

class Promising[A](val promise: Promise[A]) extends AnyVal {
  def apply[B](f: Promise[A] => B): Future[A] = {
    f(promise)
    promise.future
  }
}
