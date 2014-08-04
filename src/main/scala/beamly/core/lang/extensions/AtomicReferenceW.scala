package beamly.core.lang.extensions

import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec

final class AtomicReferenceW[A](val underlying: AtomicReference[A]) extends AnyVal {

  @inline
  def update(f: A => A) {

    @tailrec
    def updateAtomicReference(f: A => A) {
      val a = underlying.get
      if (!underlying.compareAndSet(a, f(a))) updateAtomicReference(f) else ()
    }

    updateAtomicReference(f)
  }

}
