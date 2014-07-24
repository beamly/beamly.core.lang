package beamly.core.lang.future

import scala.concurrent.{Future, Promise}

class Promising[A] {
  def apply[B](f: Promise[A] => B): Future[A] = {
    val promise = Promise[A]()
    f(promise)
    promise.future
  }
}
