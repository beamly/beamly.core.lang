/**
   Copyright (C) 2011-2014 zeebox Ltd.  http://zeebox.com

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

package zeebox.core.lang.future

import scala.concurrent.{Future, ExecutionContext}

sealed trait MaybeFuture[In] {
  type Value
  def apply(block: => In)(implicit executor: ExecutionContext): Future[Value]
  def flatMap[T](that: Future[T], f: T => In)(implicit executor: ExecutionContext): Future[Value]
}

object MaybeFuture extends MaybeFutureLowPriority {

  def apply[In](block: => In)(implicit maybeFuture: MaybeFuture[In], executionContext: ExecutionContext) = maybeFuture(block)

  implicit final def futureMaybeFuture[A] = FutureMaybeFuture.asInstanceOf[MaybeFuture[Future[A]] { type Value = A } ]

  private object FutureMaybeFuture extends MaybeFuture[Future[Any]] {
    type Value = Any
    def apply(block: => Future[Any])(implicit executionContext: ExecutionContext) = block
    def flatMap[T](that: Future[T], f: T => Future[Any])(implicit executor: ExecutionContext): Future[Any] = that flatMap f
  }
}

trait MaybeFutureLowPriority {
  implicit final def identityMaybeFuture[A] = IdentityMaybeFuture.asInstanceOf[MaybeFuture[A] { type Value = A } ]

  private object IdentityMaybeFuture extends MaybeFuture[Any] {
    type Value = Any
    def apply(block: => Any)(implicit executionContext: ExecutionContext) = Future(block)
    def flatMap[T](that: Future[T], f: T => Any)(implicit executor: ExecutionContext): Future[Any] = that map f
  }

}

