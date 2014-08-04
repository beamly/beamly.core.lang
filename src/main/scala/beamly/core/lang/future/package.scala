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

import beamly.core.lang.future.extensions._

import scala.language.implicitConversions

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise, Future, ExecutionContext}
import scala.util.{Failure, Success, Try}
import scala.util.control.{NonFatal, NoStackTrace}

case class FutureTimeoutException(duration: Duration) extends RuntimeException("Future has timed out after %s" format duration) with NoStackTrace

object `package` {

  val futureUnit: Future[Unit] = Future.successful(())

  val futureNone: Future[Option[Nothing]] = Future successful None

  /**
   * Returns a Promising[A], which can be applied on a function to fulfil a promise and return a future of that promise.
   *
   * '''Note''': if the function throws an exception, it will not be caught or fail the future.
   *
   * Example:
   *    {{{
   *      promising[A](f: Promise[A] => Any): Future[A]
   *    }}}
   *
   *    Creates a promise, uses the provided function to fulfil the promise and then returns the future of the promise.
   *
   * @tparam A The type returned
   * @return Future returned from the value
   */
  @inline def promising[A]: Promising[A] = new Promising(Promise[A]())

  @inline
  implicit def futureBeamlyLang[A](underlying: Future[A]): FutureW[A] = new FutureW(underlying)

  @inline
  implicit def futureFutureBeamlyLang[A](underlying: Future[Future[A]]): FutureFutureW[A] = new FutureFutureW(underlying)

  @inline
  implicit def futureEitherBeamlyLang[A](underlying: Future[Either[Throwable, A]]): FutureEitherW[A] = new FutureEitherW(underlying)

  @inline
  implicit def futureTryBeamlyLang[A](underlying: Future[Try[A]]): FutureTryW[A] = new FutureTryW(underlying)

  @inline
  implicit def futureOptionBeamlyLang[A](underlying: Future[Option[A]]): FutureOptionW[A] = new FutureOptionW(underlying)

  @inline
  implicit def futureTraversableBeamlyLang[A](underlying: Future[Traversable[A]]): FutureTraversableW[A] = new FutureTraversableW(underlying)

  @inline
  implicit def futureCompanionBeamlyLang(underlying: Future.type): FutureCompanionW = new FutureCompanionW(underlying)

}
