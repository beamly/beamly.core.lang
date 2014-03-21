/**
   Copyright (C) 2011-2014 beamly Ltd.  http://beamly.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

**/

package beamly.core.lang.ref

import scala.language.higherKinds

import java.lang.ref.{WeakReference => JWeakRef, SoftReference => JSoftRef}
import scala.ref.{WeakReference => SWeakRef, SoftReference => SSoftRef, ReferenceQueue}

/**
 * Type class encompassing most methods available to Java and Scala References
 *
 * Example use:
 *
 * val myRef = Ref[SoftReference] of "My String"
 *
 * Ref getOption myRef
 */
object Ref {

  def apply[M[_ <: AnyRef]](implicit refType: Ref[M]) = refType

  def get[A <: AnyRef, M[_]](ref: M[A])(implicit refType: Ref[M]) = refType get ref

  def getOrElse[A <: AnyRef, B >: A, M[_]](ref: M[A], orElse: => B)(implicit refType: Ref[M]): B = refType get ref getOrElse orElse

  def clear[M[_]](ref: M[_])(implicit refType: Ref[M]) = refType clear ref

  def isDefined[M[_]](ref: M[_])(implicit refType: Ref[M]) = refType isDefined ref

  implicit object JWeakReference extends Ref[JWeakRef] {
    def of[A <: AnyRef](value: A) = new JWeakRef(value)
    def of[A <: AnyRef](value: A, queue: ReferenceQueue[A]) = new JWeakRef(value, classOf[ReferenceQueue[A]].getMethod("underlying").invoke(queue).asInstanceOf[java.lang.ref.ReferenceQueue[A]])
    def get[A <: AnyRef](ref: JWeakRef[A]) = Option(ref.get())
    def clear(ref: JWeakRef[_]) = ref.clear()
    def isDefined(ref: JWeakRef[_]) = ref.get != null
  }

  implicit object JSoftReference extends Ref[JSoftRef] {
    def of[A <: AnyRef](value: A) = new JSoftRef(value)
    def of[A <: AnyRef](value: A, queue: ReferenceQueue[A]) = new JSoftRef(value, classOf[ReferenceQueue[A]].getMethod("underlying").invoke(queue).asInstanceOf[java.lang.ref.ReferenceQueue[A]])
    def get[A <: AnyRef](ref: JSoftRef[A]) = Option(ref.get())
    def clear(ref: JSoftRef[_]) = ref.clear()
    def isDefined(ref: JSoftRef[_]) = ref.get != null
  }

  implicit object SWeakReference extends Ref[SWeakRef] {
    def of[A <: AnyRef](value: A) = new SWeakRef(value)
    def of[A <: AnyRef](value: A, queue: ReferenceQueue[A]) = new SWeakRef(value, queue)
    def get[A <: AnyRef](ref: SWeakRef[A]) = ref.get
    def clear(ref: SWeakRef[_]) = ref.clear()
    def isDefined(ref: SWeakRef[_]) = ref.underlying.get != null
  }

  implicit object SSoftReference extends Ref[SSoftRef] {
    def of[A <: AnyRef](value: A) = new SSoftRef(value)
    def of[A <: AnyRef](value: A, queue: ReferenceQueue[A]) = new SSoftRef(value, queue)
    def get[A <: AnyRef](ref: SSoftRef[A]) = ref.get
    def clear(ref: SSoftRef[_]) = ref.clear()
    def isDefined(ref: SSoftRef[_]) = ref.underlying.get != null
  }
}

trait Ref[M[_ <: AnyRef]] {
  def of[A <: AnyRef](value: A): M[A]
  def of[A <: AnyRef](value: A, queue: ReferenceQueue[A]): M[A]
  def get[A <: AnyRef](ref: M[A]): Option[A]
  def clear(ref: M[_])
  def isDefined(ref: M[_]): Boolean
}
