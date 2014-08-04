package beamly.core.lang.extensions

final class DoubleW(val underlying: Double) extends AnyVal {
  /**
   * @return true if number is finite
   */
  @inline
  def isFinite: Boolean = !(underlying.isNaN || underlying.isInfinite)
}
