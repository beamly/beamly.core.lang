package beamly.core.lang.future
package extensions

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal


final class FutureW[+A](val underlying: Future[A]) extends AnyVal {
  /**
   * Maps a [[scala.util.Try]] to a value.
   * @param f Function which maps the [[scala.util.Try]] to a value
   * @param executor Execution context
   * @tparam B The return type
   * @return New [[scala.concurrent.Future]]
   */
  @inline
  def mapTry[B](f: Try[A] => B)(implicit executor: ExecutionContext): Future[B] = {
    promising[B] { promise =>
      underlying onComplete { x =>
        try {
          promise success f(x)
        } catch {
          case e if NonFatal(e) => promise failure e
        }
      }
    }
  }

  /**
   * Maps a [[scala.util.Try]] to a new [[scala.concurrent.Future]].
   * @param f Function which maps the [[scala.util.Try]] to a value
   * @param executor Execution context
   * @tparam B The return type
   * @return New [[scala.concurrent.Future]]
   */
  @inline
  def flatMapTry[B](f: Try[A] => Future[B])(implicit executor: ExecutionContext): Future[B] = {
    promising[B] { promise =>
      underlying onComplete { x =>
        try {
          promise completeWith f(x)
        } catch {
          case e if NonFatal(e) => promise failure e
        }
      }
    }
  }

  /**
   * @return The result from the [[scala.concurrent.Future]] after awaiting a result
   */
  @inline
  def get(): A = get(5.seconds) // Duration.Inf)

  /**
   * @param duration The amount of time to wait for the future to return
   * @return The result from the [[scala.concurrent.Future]] after awaiting a result
   */
  @inline
  def get(duration: Duration): A = Await result (underlying, duration)

  /**
   * @return The [[scala.concurrent.Future]] after awaiting a result
   */
  @inline
  def await(): Future[A] = await(5.seconds) // Duration.Inf)

  /**
   * @return The [[scala.concurrent.Future]] after awaiting a result
   */
  @inline
  def await(duration: Duration): Future[A] = {
    Await ready (underlying, duration)
  }

  /**
   * Maps successful or failed values into a new [[scala.concurrent.Future]]
   * Catches any exceptions from conversion and returns failed future.
   *
   * @param failed Function for converting a [[scala.Throwable]] to a successful value
   * @param successful Function for converting a successful value to a new success
   * @param ec The execution context
   * @tparam X The new success type
   * @return [[scala.concurrent.Future]] containing the new successful value
   */
  @inline
  def fold[X](failed: Throwable => X, successful: A => X)(implicit ec: ExecutionContext): Future[X] = {
    promising[X] { promise =>
      underlying onComplete {
        case Success(a) => try promise success successful(a) catch { case e if NonFatal(e) => promise failure e }
        case Failure(f) => try promise success failed(f) catch { case e if NonFatal(e) => promise failure e }
      }
    }
  }

  /**
   * Maps successful or failed values into a new [[scala.concurrent.Future]]
   * Catches any exceptions from conversion and returns failed future.
   *
   * @param failed Function for converting a [[scala.Throwable]] to a successful value
   * @param successful Function for converting a successful value to a new success
   * @param ec The execution context
   * @tparam X The new success type
   * @return [[scala.concurrent.Future]] containing the new successful value
   */
  @inline
  def flatFold[X](failed: Throwable => Future[X], successful: A => Future[X])(implicit ec: ExecutionContext): Future[X] = {
    promising[X] { promise =>
      underlying onComplete {
        case Success(a) => try promise completeWith successful(a) catch { case e if NonFatal(e) => promise failure e }
        case Failure(f) => try promise completeWith failed(f) catch { case e if NonFatal(e) => promise failure e }
      }
    }
  }

}