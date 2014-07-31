package beamly.core.lang.future

import scala.language.higherKinds

import scala.concurrent.Future

trait Failable[M[_, _]] {
  def success[L, R](r: R): M[L, R]
  def failure[L, R](l: L): M[L, R]
}

object Failable {
  type FutureEither[L, R] = Future[Either[L, R]]
  type FutureId[L, R]     = Future[R]
  type Id[L, R]           = R

  def apply[M[_, _]](implicit failable: Failable[M]) = failable

  implicit object FutureEitherFailable extends Failable[FutureEither] {
    def success[L, R](r: R): Future[Right[Nothing, R]] = Future.successful(Right(r))
    def failure[L, R](l: L): Future[Left[L, Nothing]]  = Future.successful(Left(l))
  }

  implicit object FutureIdFailable extends Failable[FutureId] {
    def success[L, R](r: R): Future[R]       = Future.successful(r)
    def failure[L, R](l: L): Future[Nothing] = Future.failed(l match {
      case t: Throwable => t: Throwable
      case _            => new RuntimeException(l.toString)
    })
  }

  implicit object IdFailable extends Failable[Id] {
    def success[L, R](r: R): R       = r
    def failure[L, R](l: L): Nothing = l match {
      case t: Throwable => throw t
      case _            => sys.error(l.toString)
    }
  }
}
