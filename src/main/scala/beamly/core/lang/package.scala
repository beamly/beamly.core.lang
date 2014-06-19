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


package beamly.core.lang

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
    /**
     * @return true if a string is null, empty or contains only whitespace
     */
    def isBlank = underlying == null || underlying.isEmpty || underlying.forall(Character.isWhitespace)

    /**
     * @return true if string contains non-whitespace characters
     */
    def nonBlank = !isBlank

    /**
     * Replaces word barriers with underscores.
     * "name".toSnakeCase == "name"
     * "NAME".toSnakeCase == "name"
     * "EpisodeId".toSnakeCase == "episode_id"
     * "beamLYstuff".toSnakeCase == "beam_ly_stuff"
     * "BEAMlySTUFF.toSnakeCase "beam_ly_stuff"
     * @return string with word barriers represented with underscores
     */
    def toSnakeCase: String = {
      underlying.replaceAll("([A-Z]+)([A-Z])([a-z]+)", "$1$2_$3").replaceAll("([a-z]+)([A-Z]+)", "$1_$2").toLowerCase
    }
  }

  @inline
  implicit final class EitherW[+L, +R](val underlying: Either[L, R]) extends AnyVal {
    /**
     * Successful right-hand side.
     * @return true if right
     */
    def isSuccess = underlying.isRight

    /**
     * Failed left-hand side.
     * @return true if left
     */
    def isFailure = underlying.isLeft

    /**
     * Maps successful right value.
     * @param f Mapping function
     * @tparam RR Returned right value
     * @return Either with new right value
     */
    def map[RR](f: R => RR): Either[L, RR] = underlying match {
      case Right(r) => Right(f(r))
      case left => left.asInstanceOf[Either[L, RR]]
    }

    /**
     * Maps successful value to new Either.
     * @param f Function which maps value to Either
     * @tparam LL New failed type
     * @tparam RR New successful type
     * @return New Either
     */
    def flatMap[LL >: L, RR](f: R => Either[LL, RR]): Either[LL, RR] = underlying match {
      case Right(r) => f(r)
      case left => left.asInstanceOf[Either[LL, RR]]
    }

    /**
     * Converts some failed values.
     * @param pf Partial function which converts some values to new successful
     * @tparam RR Type of new successful value
     * @return New Either
     */
    def recover[RR >: R](pf: PartialFunction[L, RR]): Either[L, RR] = underlying match {
      case Left(l) if pf isDefinedAt l => Right(pf(l))
      case other => other
    }

    /**
     * Converts some failed values.
     * @param pf Partial function which converts some values to new Either
     * @tparam RR Type of new successful value
     * @return New Either
     */
    def flatRecover[RR >: R, LL >: L](pf: PartialFunction[L, Either[LL, RR]]): Either[LL, RR] = recoverWith(pf)

    /**
     * Converts some failed values.
     * @param pf Partial function which converts some values to new Either
     * @tparam RR Type of new successful value
     * @return New Either
     */
    // consistency with other Scala APIs
    def recoverWith[RR >: R, LL >: L](pf: PartialFunction[L, Either[LL, RR]]): Either[LL, RR] = underlying match {
      case Left(l) if pf isDefinedAt l => pf(l)
      case other => other
    }

    /**
     * Gets the successful value or transforms failed values into successes.
     * @param f Function which converts failures to successes
     * @tparam RR Returned successful value
     * @return Successful value
     */
    def getOrRecover[RR >: R](f: L => RR): RR = underlying match {
      case Right(r) => r
      case Left(l) => f(l)
    }

    /**
     * Gets the successful value or another value in case of value.
     * @param other Fallback value
     * @tparam RR Returned successful value
     * @return Successful value
     */
    def getOrElse[RR >: R](other: RR): RR = underlying match {
      case Right(r) => r
      case Left(l) => other
    }

    /**
     * Filters successful values, pushing values as Left[None] which don't match the filter.
     * @param p Predicate for filtering the values
     * @param ev Conversion of failed values to Option
     * @tparam LL New failure type
     * @return New Either with successful matching filter included
     */
    def withFilter[LL](p: R => Boolean)(implicit ev: L <:< Option[LL]): Either[Option[LL], R] = underlying match {
      case Right(r) => if (p(r)) Right(r) else Left(None)
      case left => left.asInstanceOf[Either[Option[LL], R]]
    }

    /**
     * Filters successful values, pushing values as Left[None] which don't match the filter.
     * @param p Predicate for filtering the values
     * @param ev Conversion of failed values to Option
     * @tparam LL New failure type
     * @return New Either with successful matching filter included
     */
    def filter[LL](p: R => Boolean)(implicit ev: L <:< Option[LL]): Either[Option[LL], R] = withFilter(p)

    /**
     * Converts [[scala.util.Either]] to [[scala.util.Try]]
     * @param ev Way of converting failure value to [[scala.Throwable]]
     * @return Try based on success or failure of this [[scala.util.Either]]
     */
    def toTry(implicit ev: L <:< Throwable) = underlying match {
      case Right(r) => Success(r)
      case Left(l)  => Failure(l)
    }
  }

  @inline
  implicit final class OptionW[+T](val underlying: Option[T]) extends AnyVal {
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
    /**
     * Converts [[scala.util.Try]] to [[scala.concurrent.Future]]
     * @return Future from Try
     */
    def future: Future[T] = TryToFuture autoTryToFuture underlying

    /**
     * Returns successful value from underlying [[scala.util.Try]] or attempts to convert exception to value.
     * @param pf Partial function to convert exceptions to a value
     * @tparam U type of return value
     * @return Underlying value or resulting value after converting exception
     */
    def getOrRecover[U >: T](pf: PartialFunction[Throwable, U]): U = underlying match {
      case Success(s) => s
      case Failure(e) => pf.applyOrElse(e, throw (_: Throwable))
    }
  }

  // ripped from PartialFunction.scala
  private[this] val fallback_pf: PartialFunction[Any, Any] = { case _ => fallback_pf }
  private[lang] def checkFallback[B] = fallback_pf.asInstanceOf[PartialFunction[Any, B]]
  private[lang] def fallbackOccurred[B](x: B) = fallback_pf eq x.asInstanceOf[AnyRef]

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

  @inline
  implicit final class DoubleW(val underlying: Double) extends AnyVal {
    /**
     * @return true if number is finite
     */
    def isFinite: Boolean = !(underlying.isNaN || underlying.isInfinite)
  }

  def ??? : Nothing = macro notImplementedMacroImpl

  def notImplemented: Nothing = macro notImplementedMacroImpl

  def notImplementedMacroImpl(c: Context): c.Expr[Nothing] = {
    import c.universe._
    reify[Nothing](throw NotImplementedException())
  }

  implicit class MapW[K,A](val value: Map[K,A]) extends AnyVal {
    /**
     * Merges 2 maps together, using the provided merge function to create a new map value for a key.
     * @param other The other map
     * @param mergeFunction Merge function which merges values from both maps
     * @tparam B Value type of the other map
     * @tparam C Value type of merged result value
     * @return Map[K,C]
     */
    def mergeValues[B,C](other: Map[K,B])(mergeFunction: (Option[A],Option[B]) => C): Map[K, C] =
      (value.keySet ++ other.keySet).map { key =>
        key -> mergeFunction(value get key, other get key)
      }(collection.breakOut)
  }

  /** @define coll collection or iterator */
  @inline
  implicit final class TraversableWithMaxOption[+A](val xs: TraversableOnce[A]) extends AnyVal {
    /** Attempts to find the largest element.
      *
      *  @param    ord   An ordering to be used for comparing elements.
      *  @tparam   A1    The type over which the ordering is defined.
      *  @return   an option value containing the largest element of this $coll
      *            with respect to the ordering `ord`, or `None` if this $coll
      *            is empty
      *
      * @usecase def maxOption: A
      *    @inheritdoc
      *
      *    @return   an option value containing the largest element of this
      *              $coll, or `None` if this $coll is empty
      */
    def maxOption[A1 >: A](implicit ord: Ordering[A1]): Option[A] =
      if (xs.isEmpty) None else Some(xs.max[A1])
  }
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
