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

import scala.language.experimental.macros

import java.util.concurrent.atomic.AtomicReference
import scala.util.{Try, Failure, Success}
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.{PartialFunction => =?>}
import scala.reflect.macros.Context

/**
 * Useful additions to already existing Scala and Java classes. Should not include any
 * dependencies outside of the Scala and Java standard libraries.
 */
object `package` {

  @inline
  final def some[T](t: T): Option[T] = Option(t)

  @inline
  final def none[T]: Option[T] = None

  @inline
  final def nil[T]: List[T] = Nil

  @inline
  implicit final class StringW(val underlying: String) extends AnyVal {
    def isBlank = underlying == null || underlying.isEmpty || underlying.forall(Character.isWhitespace)

    def nonBlank = !isBlank
  }

  @inline
  implicit final class EitherW[+L, +R](val underlying: Either[L, R]) extends AnyVal {
    def isSuccess = underlying.isRight

    def isFailure = underlying.isLeft

    def map[RR](f: R => RR): Either[L, RR] = underlying match {
      case Right(r) => Right(f(r))
      case left => left.asInstanceOf[Either[L, RR]]
    }

    def flatMap[LL >: L, RR](f: R => Either[LL, RR]): Either[LL, RR] = underlying match {
      case Right(r) => f(r)
      case left => left.asInstanceOf[Either[LL, RR]]
    }

    def recover[RR >: R](pf: PartialFunction[L, RR]): Either[L, RR] = underlying match {
      case Left(l) if pf isDefinedAt l => Right(pf(l))
      case other => other
    }

    def flatRecover[RR >: R, LL >: L](pf: PartialFunction[L, Either[LL, RR]]): Either[LL, RR] = underlying match {
      case Left(l) if pf isDefinedAt l => pf(l)
      case other => other
    }

    // consistency with other Scala APIs
    def recoverWith[RR >: R, LL >: L](pf: PartialFunction[L, Either[LL, RR]]): Either[LL, RR] = underlying match {
      case Left(l) if pf isDefinedAt l => pf(l)
      case other => other
    }

    def getOrRecover[RR >: R](f: L => RR): RR = underlying match {
      case Right(r) => r
      case Left(l) => f(l)
    }

    def getOrElse[RR >: R](other: RR): RR = underlying match {
      case Right(r) => r
      case Left(l) => other
    }

    def withFilter[LL](p: R => Boolean)(implicit ev: L <:< Option[LL]): Either[Option[LL], R] = underlying match {
      case Right(r) => if (p(r)) Right(r) else Left(None)
      case left => left.asInstanceOf[Either[Option[LL], R]]
    }

    def filter[LL](p: R => Boolean)(implicit ev: L <:< Option[LL]): Either[Option[LL], R] = withFilter(p)

    def toTry(implicit ev: L <:< Throwable) = underlying match {
      case Right(r) => Success(r)
      case Left(l)  => Failure(l)
    }
  }

  @inline
  implicit final class OptionW[T](val underlying: Option[T]) extends AnyVal {
    /**
     * Returns the original [[scala.Option]] but allows handling of a [[scala.Some]] value, usually for logging
     * @param f The function which handles a [[scala.Some]] value
     * @return The original [[scala.Option]]
     */
    def onSome(f: T => Unit): Option[T] = {
      underlying foreach f
      underlying
    }

    /**
     * Returns the original [[scala.Option]] but allows handling of the [[scala.None]] value, usually for logging
     * @param f The function which handles a [[scala.None]] value
     * @return The original [[scala.Option]]
     */
    def onNone(f: => Unit): Option[T] = {
      if (underlying.isEmpty)
        f
      underlying
    }
  }

  @tailrec
  private def sendAtomicReference[A](ref: AtomicReference[A])(f: A => A) {
    val a = ref.get
    if (!ref.compareAndSet(a, f(a))) sendAtomicReference(ref)(f) else ()
  }

  @inline
  implicit final class AtomicReferenceW[A](val underlying: AtomicReference[A]) extends AnyVal {
    def send(f: A => A) {
      sendAtomicReference(underlying)(f)
    }
  }

  @inline
  implicit final class TryW[T](val underlying: Try[T]) extends AnyVal {
    def future: Future[T] = TryToFuture autoTryToFuture underlying

    def getOrRecover[U >: T](pf: PartialFunction[Throwable, U]): U = underlying match {
      case Success(s) => s
      case Failure(e) => pf.applyOrElse(e, throw (_: Throwable))
    }
  }

  // ripped from PartialFunction.scala
  private[this] val fallback_pf: PartialFunction[Any, Any] = { case _ => fallback_pf }
  private def checkFallback[B] = fallback_pf.asInstanceOf[PartialFunction[Any, B]]
  private def fallbackOccurred[B](x: B) = (fallback_pf eq x.asInstanceOf[AnyRef])

  implicit class PartialFunctionW[-A, +B](val pf: A =?> B) extends AnyVal {

    /**
     * The same as 'andThen', except the input object is passed as well as the result.
     */
    def andThenWithContext[A1 <: A, C](k: (A1, B) => C): A1 =?> C = new AndThenWithContext(pf, k)

    /**
     * The same as 'compose' but returns a PartialFunction
     */
    def composePF[C](k: C => A): C =?> B = new ComposePF(k, pf)

  }

  private class AndThenWithContext[-A, B, +C](pf: A =?> B, k: (A, B) => C) extends (A =?> C) {
    def apply(x: A): C = k(x, pf(x))

    def isDefinedAt(x: A): Boolean = pf isDefinedAt x

    override def applyOrElse[A1 <: A, C1 >: C](x: A1, default: A1 => C1): C1 = {
      val z = pf.applyOrElse(x, checkFallback[B])
      if (!fallbackOccurred(z)) k(x, z) else default(x)
    }
  }

  private class ComposePF[-A, B, +C](k: (A => B), pf: B =?> C) extends (A =?> C) {
    def apply(x: A): C = pf(k(x))

    def isDefinedAt(x: A): Boolean = pf isDefinedAt k(x)

    override def applyOrElse[A1 <: A, C1 >: C](x: A1, default: A1 => C1): C1 = {
      val b = k(x)
      val z = pf.applyOrElse(b, checkFallback[C])
      if (!fallbackOccurred(z)) z else default(x)
    }
  }

  @inline
  implicit final class DoubleW(val underlying: Double) extends AnyVal {
    def isFinite: Boolean = !(underlying.isNaN || underlying.isInfinite)
  }

  def ??? : Nothing = macro notImplementedMacroImpl

  def notImplemented: Nothing = macro notImplementedMacroImpl

  def notImplementedMacroImpl(c: Context): c.Expr[Nothing] = {
    import c.universe._
    reify[Nothing](throw NotImplementedException())
  }

  implicit class MapW[K,A](val value: Map[K,A]) extends AnyVal {
    def mergeValues[B,C](other: Map[K,B])(mergeFunction: (Option[A],Option[B]) => C): Map[K, C] =
      (value.keySet ++ other.keySet).map { key =>
        key -> mergeFunction(value get key, other get key)
      }(collection.breakOut)
  }

}
