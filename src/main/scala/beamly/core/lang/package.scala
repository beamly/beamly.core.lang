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

import beamly.core.lang.extensions.{TraversableSafer, RightBiasedEither}

import scala.language.experimental.macros
import scala.language.implicitConversions

import java.util.concurrent.atomic.AtomicReference
import scala.util.{Try, Failure, Success}
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.{PartialFunction => =?>}
import scala.reflect.macros.Context
import scala.util.control.Exception.catching

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
  implicit final class StringBeamlyLang(val underlying: String) extends AnyVal {
    /**
     * @return true if a string is null, empty or contains only whitespace
     */
    def isBlank = underlying == null || underlying.isEmpty || underlying.forall(Character.isWhitespace)

    /**
     * @return true if string contains non-whitespace characters
     */
    def nonBlank = !isBlank

    /**
     * Replaces word barriers with underscores and converts entire string to lowercase.
     * "name".toSnakeCase == "name"
     * "NAME".toSnakeCase == "name"
     * "EpisodeId".toSnakeCase == "episode_id"
     * "beamLYstuff".toSnakeCase == "beam_ly_stuff"
     * "BEAMlySTUFF.toSnakeCase "beam_ly_stuff"
     * @return string with word barriers represented with underscores
     */
    def toSnakeCase: String = replaceWordBoundary("_")

    /**
     * Replaces word barriers with hyphens (and by "hyphens" what is actually meant is "hyphen-minus", ie. U+002D) and
     * converts entire string to lowercase.
     * {{{
     * "name".toHyphenCase == "name"
     * "NAME".toHyphenCase == "name"
     * "EpisodeId".toHyphenCase == "episode-id"
     * "beamLYstuff".toHyphenCase == "beam-ly-stuff"
     * "BEAMlySTUFF.toHyphenCase "beam-ly-stuff"
     * }}}
     * @return string with word barriers represented with hyphens
     */
    def toHyphenCase: String = replaceWordBoundary("-")

    /**
     * Replaces word barriers with provided string and converts entire string to lowercase.
     * {{{
     * "name".replaceWordBoundary("|") == "name"
     * "NAME".replaceWordBoundary("|") == "name"
     * "EpisodeId".replaceWordBoundary("|") == "episode|id"
     * "beamLYstuff".replaceWordBoundary("|") == "beam|ly|stuff"
     * "BEAMlySTUFF.replaceWordBoundary("|") "beam|ly|stuff"
     * }}}
     * @return string with word barriers represented with hyphens
     */
    private def replaceWordBoundary(replacementString: String): String = {
      underlying
        .replaceAll("([A-Z]+)([A-Z])([a-z]+)", "$1$2" + replacementString + "$3")
        .replaceAll("([a-z]+)([A-Z]+)", "$1" + replacementString + "$2")
        .toLowerCase
    }

    def toBooleanOption = catching(classOf[IllegalArgumentException]) opt underlying.toBoolean
    def toByteOption    = catching(classOf[NumberFormatException]) opt underlying.toByte
    def toShortOption   = catching(classOf[NumberFormatException]) opt underlying.toShort
    def toIntOption     = catching(classOf[NumberFormatException]) opt underlying.toInt
    def toLongOption    = catching(classOf[NumberFormatException]) opt underlying.toLong
    def toFloatOption   = catching(classOf[NumberFormatException]) opt underlying.toFloat
    def toDoubleOption  = catching(classOf[NumberFormatException]) opt underlying.toDouble
  }

  @inline
  implicit def rightBiasedEither[L, R](either: Either[L, R]) = new RightBiasedEither[L, R](either)

  implicit final class OptionBeamlyLang[+T](val underlying: Option[T]) extends AnyVal {
    /**
     * Returns the original [[scala.Option]] but allows handling of a [[scala.Some]] value, usually for logging
     * @param f The function which handles a [[scala.Some]] value
     * @return The original [[scala.Option]]
     */
    @inline
    def onSome(f: T => Unit): Option[T] = {
      underlying foreach f
      underlying
    }

    /**
     * Returns the original [[scala.Option]] but allows handling of the [[scala.None]] value, usually for logging
     * @param f The function which handles a [[scala.None]] value
     * @return The original [[scala.Option]]
     */
    @inline
    def onNone(f: => Unit): Option[T] = {
      if (underlying.isEmpty)
        f
      underlying
    }
  }

  implicit final class AtomicReferenceUpdate[A](val underlying: AtomicReference[A]) extends AnyVal {

    def update(f: A => A) {

      @tailrec
      def updateAtomicReference(f: A => A) {
        val a = underlying.get
        if (!underlying.compareAndSet(a, f(a))) updateAtomicReference(f) else ()
      }

      updateAtomicReference(f)
    }

  }

  implicit final class TryBeamlyLang[T](val underlying: Try[T]) extends AnyVal {
    /**
     * Converts [[scala.util.Try]] to [[scala.concurrent.Future]]
     * @return Future from Try
     */
    @inline
    def future: Future[T] = TryToFuture autoTryToFuture underlying

    /**
     * Returns successful value from underlying [[scala.util.Try]] or attempts to convert exception to value.
     * @param pf Partial function to convert exceptions to a value
     * @tparam U type of return value
     * @return Underlying value or resulting value after converting exception
     */
    @inline
    def getOrRecover[U >: T](pf: => PartialFunction[Throwable, U]): U = underlying match {
      case Success(s) => s
      case Failure(e) => pf.applyOrElse(e, throw (_: Throwable))
    }
  }

  // ripped from PartialFunction.scala
  private[this] val fallback_pf: PartialFunction[Any, Any] = { case _ => fallback_pf }
  @inline private[lang] def checkFallback[B] = fallback_pf.asInstanceOf[PartialFunction[Any, B]]
  @inline private[lang] def fallbackOccurred[B](x: B) = fallback_pf eq x.asInstanceOf[AnyRef]

  implicit final class PartialFunctionBeamlyLang[-A, +B](val pf: A =?> B) extends AnyVal {

    /**
     * The same as 'andThen', except the input object is passed as well as the result.
     */
    def andThenWithContext[A1 <: A, C](k: (A1, B) => C): A1 =?> C = new AndThenWithContext(pf, k)

    /**
     * The same as 'compose' but returns a PartialFunction
     */
    def composePF[C](k: C => A): C =?> B = new ComposePF(k, pf)

  }

  implicit final class DoubleBeamlyLang(val underlying: Double) extends AnyVal {
    /**
     * @return true if number is finite
     */
    @inline
    def isFinite: Boolean = !(underlying.isNaN || underlying.isInfinite)
  }

  /**
   * Implemented as a macro so the stack trace begins where '???' is called.
   */
  def ??? : Nothing = macro notImplementedMacroImpl

  def notImplemented: Nothing = macro notImplementedMacroImpl

  def notImplementedMacroImpl(c: Context): c.Expr[Nothing] = {
    import c.universe._
    reify[Nothing](throw NotImplementedException())
  }

  implicit class MapBeamlyLang[K,A](val value: Map[K,A]) extends AnyVal {
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

  @inline
  implicit def traversableSafer[A](xs: TraversableOnce[A]) = new TraversableSafer[A](xs)
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
