package beamly.core.lang.extensions

final class TraversableSafer[+A](val xs: TraversableOnce[A]) extends AnyVal {
  /** Attempts to find the largest element.
    *
    *  @param    ord   An ordering to be used for comparing elements.
    *  @tparam   A1    The type over which the ordering is defined.
    *  @return   an option value containing the largest element of this $coll
    *            with respect to the ordering `ord`, or `None` if this $coll
    *            is empty
    *
    * @usecase def maxOption: A
    *    @inheritdoc
    *
    *    @return   an option value containing the largest element of this
    *              $coll, or `None` if this $coll is empty
    */
  @inline
  def maxOption[A1 >: A](implicit ord: Ordering[A1]): Option[A] =
    if (xs.isEmpty) None else Some(xs.max[A1](ord))

  @inline
  def minOption[A1 >: A](implicit ord: Ordering[A1]): Option[A] =
    if (xs.isEmpty) None else Some(xs.min[A1](ord))

}
