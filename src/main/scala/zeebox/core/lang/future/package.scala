/**
   Copyright (C) 2011-2014 zeebox Ltd.  http://zeebox.com

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

package zeebox.core.lang

import scala.language.implicitConversions
import scala.language.experimental.macros

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise, Future, ExecutionContext}
import scala.util.{Failure, Success, Try}
import scala.util.control.{NonFatal, NoStackTrace}
import scala.reflect.macros.Context

package object future {

  case class FutureTimeoutException(duration: Duration) extends RuntimeException("Future has timed out after %s" format duration) with NoStackTrace

  val futureUnit: Future[Unit] = Future.successful()

  val futureNone: Future[Option[Nothing]] = Future successful None

  implicit class FutureW[A](val underlying: Future[A]) extends AnyVal {
    def mapTry[B](f: Try[A] => B)(implicit executor: ExecutionContext): Future[B] = {
      val promise = Promise[B]()
      underlying onComplete { x =>
        try {
          promise success f(x)
        } catch {
          case e if NonFatal(e) => promise failure e
        }
      }
      promise.future
    }

    def flatMapTry[B](f: Try[A] => Future[B])(implicit executor: ExecutionContext): Future[B] = {
      val promise = Promise[B]()
      underlying onComplete { x =>
        try {
          promise completeWith f(x)
        } catch {
          case e if NonFatal(e) => promise failure e
        }
      }
      promise.future
    }

    def get(): A = get(5.seconds) // Duration.Inf)

    def get(duration: Duration): A = Await result (underlying, duration)

    def await(): Future[A] = await(5.seconds) // Duration.Inf)

    def await(duration: Duration): Future[A] = {
      Await ready (underlying, duration)
    }

    def fold[X](failed: Throwable => X, successful: A => X)(implicit ec: ExecutionContext): Future[X] = {
      val promise = Promise[X]()
      underlying onComplete {
        case Success(a) => try promise success successful(a) catch { case e if NonFatal(e) => promise failure e }
        case Failure(f) => try promise success failed(f) catch { case e if NonFatal(e) => promise failure e }
      }
      promise.future
    }

    def flatFold[X](failed: Throwable => Future[X], successful: A => Future[X])(implicit ec: ExecutionContext): Future[X] = {
      val promise = Promise[X]()
      underlying onComplete {
        case Success(a) => try promise completeWith successful(a) catch { case e if NonFatal(e) => promise failure e }
        case Failure(f) => try promise completeWith failed(f) catch { case e if NonFatal(e) => promise failure e }
      }
      promise.future
    }

  }

  implicit class FutureFutureW[A](val underlying: Future[Future[A]]) extends AnyVal {
    def join(implicit executor: ExecutionContext) = underlying flatMap identity
    def flatten(implicit executor: ExecutionContext) = underlying flatMap identity
  }

  implicit class FutureEitherW[A](val underlying: Future[Either[Throwable, A]]) extends AnyVal {
    def join(implicit executor: ExecutionContext) = underlying flatMap (_.fold(Future.failed, Future.successful))
  }

  implicit class FutureTryW[A](val underlying: Future[Try[A]]) extends AnyVal {
    def join(implicit executor: ExecutionContext) = underlying flatMap {
      case Success(s) => Future successful s
      case Failure(e) => Future failed e
    }
  }

  implicit class FutureOptionW[A](val underlying: Future[Option[A]]) extends AnyVal {
    def mapOpt[B](f: A => B)(implicit executor: ExecutionContext): Future[Option[B]] =
      underlying map (_ map f)

    def flatMapOpt[B](f: A => Future[Option[B]])(implicit executor: ExecutionContext): Future[Option[B]] =
      underlying flatMap (_ map f getOrElse futureNone)
  }

  implicit class FutureCompanionW(val underyling: Future.type) extends AnyVal {
    def of[A](a: A)(implicit ec: ExecutionContext): Future[A] = macro smartFutureMacroImpl[A]
  }

  def smartFutureMacroImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A])(ec: c.Expr[ExecutionContext]): c.Expr[Future[A]] = {
    import c.universe._

    val futureSelect = Select(Select(Ident(newTermName("scala")), newTermName("concurrent")), newTermName("Future"))
    val futureSuccessful = Select(futureSelect, newTermName("successful"))
    val futureApply = Select(futureSelect, newTermName("apply"))

    a.tree match {
      case _: Literal | _: Ident => c.Expr[Future[A]](Apply(futureSuccessful, a.tree :: Nil))
      case _                     => c.Expr[Future[A]](Apply(Apply(futureApply, a.tree :: Nil), ec.tree :: Nil))
    }
  }
}
