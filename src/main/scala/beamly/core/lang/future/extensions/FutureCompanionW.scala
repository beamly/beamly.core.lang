package beamly.core.lang.future.extensions

import scala.language.experimental.macros

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.macros.Context
import beamly.core.lang._

object FutureCompanionW {
  def smartFutureMacroImpl[A: c.WeakTypeTag](c: Context)(a: c.Expr[A])(ec: c.Expr[ExecutionContext]): c.Expr[Future[A]] = {
    import c.universe._

    a.tree match {
      case _: Literal | _: Ident => c.Expr[Future[A]](q"scala.concurrent.Future.successful($a)")
      case _                     => c.Expr[Future[A]](q"scala.concurrent.Future($a)")
    }
  }
}

class FutureCompanionW(val underlying: Future.type) extends AnyVal {
  def of[A](a: A)(implicit ec: ExecutionContext): Future[A] = macro FutureCompanionW.smartFutureMacroImpl[A]

  /**
   * Creates a Future Option based on the provided option.
   * @param optionalValue The optional value
   * @param f Function to convert some value to a future
   * @param ec The execution context
   * @tparam A The type of the optional value
   * @tparam B The return type
   * @return Future None if the optional value is None, otherwise Future Some value
   */
  @inline
  def option[A,B](optionalValue: Option[A])(f: A => Future[B])(implicit ec: ExecutionContext): Future[Option[B]] = {
    optionalValue.fold(Future successful none[B]){ value =>
      f(value) map (Some(_))
    }
  }
}
