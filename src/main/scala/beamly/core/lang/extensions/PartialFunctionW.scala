package beamly.core.lang.extensions

import scala.{PartialFunction => =?>}

object PartialFunctionW {
  private[this] val fallback_pf: PartialFunction[Any, Any] = { case _ => fallback_pf }
  @inline private[lang] def checkFallback[B] = fallback_pf.asInstanceOf[PartialFunction[Any, B]]
  @inline private[lang] def fallbackOccurred[B](x: B) = fallback_pf eq x.asInstanceOf[AnyRef]

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
}

final class PartialFunctionW[-A, +B](val pf: A =?> B) extends AnyVal {
  import PartialFunctionW.{ComposePF, AndThenWithContext}

  /**
   * The same as 'andThen', except the input object is passed as well as the result.
   */
  def andThenWithContext[A1 <: A, C](k: (A1, B) => C): A1 =?> C = new AndThenWithContext(pf, k)

  /**
   * The same as 'compose' but returns a PartialFunction
   */
  def composePF[C](k: C => A): C =?> B = new ComposePF(k, pf)

}
