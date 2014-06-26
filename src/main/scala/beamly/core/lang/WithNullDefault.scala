package beamly.core.lang

object WithNullDefault {
  @inline
  implicit def toWithNullDefault[A <: AnyRef](a: A) = new WithNullDefault[A](a)
}

final class WithNullDefault[A <: AnyRef](private val underlying: A) extends AnyVal {

  @inline
  def withNullDefault[B >: A](default: B): B = {
    val value = underlying
    if (value eq null) default else value
  }
}
