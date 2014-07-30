package beamly.core.lang.future

import scala.language.higherKinds

import beamly.core.lang.future.Failable._
import org.specs2.mutable.Specification

import scala.util.{Failure, Success}

class FailableTest extends Specification {
  val arithmeticException = new ArithmeticException("It's a zero!")

  def foo[M[_, _]: Failable](n: Int): M[ArithmeticException, Int] =
    if (n == 0)
      Failable[M].failure(arithmeticException)
    else
      Failable[M].success(n)

  "FutureEitherFailable" should {
    "handle success" in foo[FutureEither](1).value ==== Some(Success(Right(1)))
    "handle failure" in foo[FutureEither](0).value ==== Some(Success(Left(arithmeticException)))
  }
  "FutureIdFailable" should {
    "handle success" in foo[FutureId](1).value ==== Some(Success(1))
    "handle failure" in foo[FutureId](0).value ==== Some(Failure(arithmeticException))
  }
  "IdFailable" should {
    "handle success" in foo[Id](1) ==== 1
    "handle failure" in (foo[Id](0) must throwA(arithmeticException))
  }

  class SomeClient[M[_, _]: Failable] {
    def foo(n: Int): M[ArithmeticException, Int] =
      if (n == 0)
        Failable[M].failure(arithmeticException)
      else
        Failable[M].success(n)
  }

  "SomeClient1[FutureEither]" should {
    val someClient = new SomeClient[FutureEither]
    "handle success" in someClient.foo(1).value ==== Some(Success(Right(1)))
    "handle failure" in someClient.foo(0).value ==== Some(Success(Left(arithmeticException)))
  }
  "SomeClient1[FutureId]" should {
    val someClient = new SomeClient[FutureId]
    "handle success" in someClient.foo(1).value ==== Some(Success(1))
    "handle failure" in someClient.foo(0).value ==== Some(Failure(arithmeticException))
  }
  "SomeClient1[Id]" should {
    val someClient = new SomeClient[Id]
    "handle success" in someClient.foo(1) ==== 1
    "handle failure" in (someClient.foo(0) must throwA(arithmeticException))
  }
}
