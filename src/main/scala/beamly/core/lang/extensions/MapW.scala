package beamly.core.lang.extensions

final class MapW[K,A](val value: Map[K,A]) extends AnyVal {
  /**
   * Merges 2 maps together, using the provided merge function to create a new map value for a key.
   * @param other The other map
   * @param mergeFunction Merge function which merges values from both maps
   * @tparam B Value type of the other map
   * @tparam C Value type of merged result value
   * @return Map[K,C]
   */
  def mergeValues[B,C](other: Map[K,B])(mergeFunction: (Option[A],Option[B]) => C): Map[K, C] =
    (value.keySet ++ other.keySet).map { key =>
      key -> mergeFunction(value get key, other get key)
    }(collection.breakOut)
}
