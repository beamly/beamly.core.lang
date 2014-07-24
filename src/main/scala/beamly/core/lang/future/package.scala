/**
   Copyright (C) 2011-2014 beamly Ltd.  http://beamly.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

**/


package beamly.core.lang.future

import scala.language.implicitConversions
import scala.language.experimental.macros

import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise, Future, ExecutionContext}
import scala.util.{Failure, Success, Try}
import scala.util.control.{NonFatal, NoStackTrace}
import scala.reflect.macros.Context

case class FutureTimeoutException(duration: Duration) extends RuntimeException("Future has timed out after %s" format duration) with NoStackTrace

object `package` {

  val futureUnit: Future[Unit] = Future.successful(())

  val futureNone: Future[Option[Nothing]] = Future successful None

  /**
   * Returns a Promising[A], which can be applied on a function to fulfil a promise and return a future of that promise.
   *
   * '''Note''': if the function throws an exception, it will not be caught or fail the future.
   *
   * @usecase promising[A](f: Promise[A] => Any): Future[A]
   *
   *    Creates a promise, uses the provided function to fulfil the promise and then returns the future of the promise.
   *
   *    '''Note''': if the function throws an exception, it will not be caught or fail the future.
   *
   *    @param f The function used to fulfil the promise
   *    @tparam A The type returned
   *    @return Future returned from the value
   *
   * @tparam A The type returned
   * @return Future returned from the value
   */
  def promising[A]: Promising[A] = new Promising

  class Promising[A] {
    def apply[B](f: Promise[A] => B): Future[A] = {
      val promise = Promise[A]()
      f(promise)
      promise.future
    }
  }

  implicit class FutureW[+A](val underlying: Future[A]) extends AnyVal {
    /**
     * Maps a [[scala.util.Try]] to a value.
     * @param f Function which maps the [[scala.util.Try]] to a value
     * @param executor Execution context
     * @tparam B The return type
     * @return New [[scala.concurrent.Future]]
     */
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
    def get(): A = get(5.seconds) // Duration.Inf)

    /**
     * @param duration The amount of time to wait for the future to return
     * @return The result from the [[scala.concurrent.Future]] after awaiting a result
     */
    def get(duration: Duration): A = Await result (underlying, duration)

    /**
     * @return The [[scala.concurrent.Future]] after awaiting a result
     */
    def await(): Future[A] = await(5.seconds) // Duration.Inf)

    /**
     * @return The [[scala.concurrent.Future]] after awaiting a result
     */
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
    def flatFold[X](failed: Throwable => Future[X], successful: A => Future[X])(implicit ec: ExecutionContext): Future[X] = {
      promising[X] { promise =>
        underlying onComplete {
          case Success(a) => try promise completeWith successful(a) catch { case e if NonFatal(e) => promise failure e }
          case Failure(f) => try promise completeWith failed(f) catch { case e if NonFatal(e) => promise failure e }
        }
      }
    }

  }

  implicit class FutureFutureW[+A](val underlying: Future[Future[A]]) extends AnyVal {
    /**
     * @param executor The execution context
     * @return Flattened [[scala.concurrent.Future]]
     */
    def join(implicit executor: ExecutionContext): Future[A] = underlying flatMap identity

    /**
     * @param executor The execution context
     * @return Flattened [[scala.concurrent.Future]]
     */
    def flatten(implicit executor: ExecutionContext): Future[A] = underlying flatMap identity
  }

  implicit class FutureEitherW[+A](val underlying: Future[Either[Throwable, A]]) extends AnyVal {
    /**
     * Converts to successful or failed future
     * @param executor The execution context
     * @return Future, mapping left to failed Future and right to successful Future
     */
    def join(implicit executor: ExecutionContext): Future[A] = underlying flatMap (_.fold(Future.failed, Future.successful))
  }

  implicit class FutureTryW[+A](val underlying: Future[Try[A]]) extends AnyVal {
    /**
     * Flattens to future.
     * @param executor The execution context
     * @return Future completed with [[scala.util.Try]] value
     */
    def join(implicit executor: ExecutionContext): Future[A] = underlying flatMap {
      case Success(s) => Future successful s
      case Failure(e) => Future failed e
    }
  }

  implicit class FutureOptionW[+A](val underlying: Future[Option[A]]) extends AnyVal {
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
    def mapOpt[B](f: A => B)(implicit executor: ExecutionContext): Future[Option[B]] =
      underlying map (_ map f)

    def flatMapOpt[B](f: A => Future[Option[B]])(implicit executor: ExecutionContext): Future[Option[B]] =
      underlying flatMap (_ map f getOrElse futureNone)

    /**
     * If this future value is none, then use the other future value.
     * @param other Function used to get another future option, if the original was a future none
     * @param executor The execution context
     * @tparam B The new return type
     * @return A future option based on the original or other future calculation
     */
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

  implicit class FutureTraversableW[+A](val underlying: Future[Traversable[A]]) extends AnyVal {
    /**
     * Maps values in collection, creating new future based on the mapped results
     * @param f Function to map values in collection
     * @param cbf [[scala.collection.generic.CanBuildFrom]] to convert to new type of collection
     * @param executor The execution context
     * @tparam B The return type of the collection elements
     * @tparam CB The collection type returned
     * @return New future with mapped collection values
     */
    def mapTraversable[B,CB](f: A => B)(implicit cbf: CanBuildFrom[Traversable[A], B, CB], executor: ExecutionContext): Future[CB] = {
      underlying.map(_ map f)
    }
  }

  implicit class FutureCompanionW(val underlying: Future.type) extends AnyVal {
    def of[A](a: A)(implicit ec: ExecutionContext): Future[A] = macro smartFutureMacroImpl[A]

    /**
     * Creates a Future Option based on the provided option.
     * @param optionalValue The optional value
     * @param f Function to convert some value to a future
     * @param ec The execution context
     * @tparam A The type of the optional value
     * @tparam B The return type
     * @return Future None if the optional value is None, otherwise Future Some value
     */
    def option[A,B](optionalValue: Option[A])(f: A => Future[B])(implicit ec: ExecutionContext): Future[Option[B]] = {
      optionalValue map { value =>
        f(value) map (Some(_))
      } getOrElse Future.successful(None)
    }
  }

  def smartFutureMacroImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A])(ec: c.Expr[ExecutionContext]): c.Expr[Future[A]] = {
    import c.universe._

    a.tree match {
      case _: Literal | _: Ident => c.Expr[Future[A]](q"scala.concurrent.Future.successful($a)")
      case _                     => c.Expr[Future[A]](q"scala.concurrent.Future($a)")
    }
  }
}
