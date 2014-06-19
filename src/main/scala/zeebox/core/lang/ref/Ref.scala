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

import java.lang.ref.{WeakReference => JWeakRef, SoftReference => JSoftRef, ReferenceQueue => JRefQueue}
import scala.ref.{WeakReference => SWeakRef, SoftReference => SSoftRef, ReferenceQueue => SRefQueue}

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

  def apply[M[_ <: AnyRef]](implicit refType: Ref[M]): Ref[M] = refType

  def get[A <: AnyRef, M[_]: Ref](ref: M[A]): Option[A] = Ref[M] get ref

  def getOrElse[A <: AnyRef, B >: A, M[_]: Ref](ref: M[A], orElse: => B): B = Ref[M] get ref getOrElse orElse

  def clear[M[_]: Ref](ref: M[_]): Unit = Ref[M] clear ref

  def isDefined[M[_]: Ref](ref: M[_]): Boolean = Ref[M] isDefined ref

  private def getJRefQueue[A <: AnyRef](queue: SRefQueue[A]): JRefQueue[A] =
    classOf[SRefQueue[A]].getMethod("underlying").invoke(queue).asInstanceOf[JRefQueue[A]]

  implicit object JWeakReference extends Ref[JWeakRef] {
    def of[A <: AnyRef](value: A) = new JWeakRef(value)
    def of[A <: AnyRef](value: A, queue: SRefQueue[A]) = new JWeakRef(value, getJRefQueue(queue))
    def get[A <: AnyRef](ref: JWeakRef[A]) = Option(ref.get())
    def clear(ref: JWeakRef[_]) = ref.clear()
    def isDefined(ref: JWeakRef[_]) = ref.get != null
  }

  implicit object JSoftReference extends Ref[JSoftRef] {
    def of[A <: AnyRef](value: A) = new JSoftRef(value)
    def of[A <: AnyRef](value: A, queue: SRefQueue[A]) = new JSoftRef(value, getJRefQueue(queue))
    def get[A <: AnyRef](ref: JSoftRef[A]) = Option(ref.get())
    def clear(ref: JSoftRef[_]) = ref.clear()
    def isDefined(ref: JSoftRef[_]) = ref.get != null
  }

  implicit object SWeakReference extends Ref[SWeakRef] {
    def of[A <: AnyRef](value: A) = new SWeakRef(value)
    def of[A <: AnyRef](value: A, queue: SRefQueue[A]) = new SWeakRef(value, queue)
    def get[A <: AnyRef](ref: SWeakRef[A]) = ref.get
    def clear(ref: SWeakRef[_]) = ref.clear()
    def isDefined(ref: SWeakRef[_]) = ref.underlying.get != null
  }

  implicit object SSoftReference extends Ref[SSoftRef] {
    def of[A <: AnyRef](value: A) = new SSoftRef(value)
    def of[A <: AnyRef](value: A, queue: SRefQueue[A]) = new SSoftRef(value, queue)
    def get[A <: AnyRef](ref: SSoftRef[A]) = ref.get
    def clear(ref: SSoftRef[_]) = ref.clear()
    def isDefined(ref: SSoftRef[_]) = ref.underlying.get != null
  }
}

trait Ref[M[_ <: AnyRef]] {
  def of[A <: AnyRef](value: A): M[A]
  def of[A <: AnyRef](value: A, queue: SRefQueue[A]): M[A]
  def get[A <: AnyRef](ref: M[A]): Option[A]
  def clear(ref: M[_])
  def isDefined(ref: M[_]): Boolean
}
