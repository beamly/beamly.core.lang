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

import beamly.core.lang.extensions._

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
  implicit def anyBeamlyLang[A](underlying: A): AnyW[A] = new AnyW[A](underlying)

  @inline
  implicit def stringBeamlyLang(underlying: String): StringW = new StringW(underlying)

  @inline
  implicit def rightBiasedEither[L, R](either: Either[L, R]) = new RightBiasedEither[L, R](either)

  @inline
  implicit def optionBeamlyLang[T](underlying: Option[T]): OptionW[T] = new OptionW(underlying)

  @inline
  implicit def atomicReferenceBeamlyLang[A](underlying: AtomicReference[A]): AtomicReferenceW[A] = new AtomicReferenceW[A](underlying)

  @inline
  implicit def tryBeamlyLang[A](underlying: Try[A]): TryW[A] = new TryW[A](underlying)

  @inline
  implicit def partialFunctionBeamlyLang[A, B](underlying: PartialFunction[A, B]): PartialFunctionW[A, B] = new PartialFunctionW(underlying)

  @inline
  implicit def doubleBeamlyLang(underlying: Double): DoubleW = new DoubleW(underlying)

  @inline
  implicit def mapBeamlyLang[A, B](underlying: Map[A, B]): MapW[A, B] = new MapW(underlying)

  @inline
  implicit def traversableSafer[A](xs: TraversableOnce[A]) = new TraversableSafer[A](xs)

  /**
   * Implemented as a macro so the stack trace begins where '???' is called.
   */
  def ??? : Nothing = macro notImplementedMacroImpl

  def notImplemented: Nothing = macro notImplementedMacroImpl

  def notImplementedMacroImpl(c: Context): c.Expr[Nothing] = {
    import c.universe._
    reify[Nothing](throw NotImplementedException())
  }
}
