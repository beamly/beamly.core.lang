package beamly.core.lang.extensions

import scala.util.{Failure, Success}

final class RightBiasedEither[+L, +R](val underlying: Either[L, R]) extends AnyVal {
  /**
   * Successful right-hand side.
   * @return true if right
   */
  @inline
  def isSuccess = underlying.isRight

  /**
   * Failed left-hand side.
   * @return true if left
   */
  @inline
  def isFailure = underlying.isLeft

  /**
   * Maps successful right value.
   * @param f Mapping function
   * @tparam RR Returned right value
   * @return Either with new right value
   */
  @inline
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
  @inline
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
  @inline
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
  @inline
  def flatRecover[RR >: R, LL >: L](pf: PartialFunction[L, Either[LL, RR]]): Either[LL, RR] = recoverWith(pf)

  /**
   * Converts some failed values.
   * @param pf Partial function which converts some values to new Either
   * @tparam RR Type of new successful value
   * @return New Either
   */
  @inline
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
  @inline
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
  @inline
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
  @inline
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
  @inline
  def filter[LL](p: R => Boolean)(implicit ev: L <:< Option[LL]): Either[Option[LL], R] = withFilter(p)

  /**
   * Converts [[scala.util.Either]] to [[scala.util.Try]]
   * @param ev Way of converting failure value to [[scala.Throwable]]
   * @return Try based on success or failure of this [[scala.util.Either]]
   */
  @inline
  def toTry(implicit ev: L <:< Throwable) = underlying match {
    case Right(r) => Success(r)
    case Left(l)  => Failure(l)
  }
}
