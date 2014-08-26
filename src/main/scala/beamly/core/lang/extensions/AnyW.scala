package beamly.core.lang.extensions

import scala.{PartialFunction => =?>}

final class AnyW[A](val underlying: A) extends AnyVal {
  /**
   * Tries to match the lhs with the specified partial function, wrapping the result in an [[scala.Option Option]].
   *
   * @param pf The partial function to match against
   * @return [[scala.None None]] if the partial function doesn't apply, a [[scala.Some Some]]-wrapped value if it does
   */
  @inline def matchOption[B](pf: A =?> B): Option[B] = pf.lift apply underlying
}
