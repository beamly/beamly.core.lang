package beamly.core.lang.extensions

final class OptionW[+T](val underlying: Option[T]) extends AnyVal {
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
