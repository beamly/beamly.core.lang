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

import scala.concurrent.duration.Duration
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}
import java.util.{concurrent => juc}
import scala.concurrent.{ExecutionContext, Promise, Future}
import annotation.tailrec

/**
 * A helper that polls for the completion of Java Futures. The results of the Java Futures
 * are returned through Akka Futures.
 *
 * @param sleepTime The duration that the selector will wait between each iteration
 */
class FutureSelector(sleepTime: Duration) extends Thread {
  private[this] val _futures = new AtomicReference(Map.empty[juc.Future[Any], Promise[Any]])
  private[this] val _run = new AtomicBoolean(false)
  private[this] val _shutdown = new AtomicBoolean(false)
  private[this] val _sleepTimeMs = sleepTime.toMillis

  @tailrec
  private def add(jfuture: juc.Future[Any], promise: Promise[Any]) {
    val futures = _futures.get
    if (!_futures.compareAndSet(futures, futures + (jfuture -> promise)))
      add(jfuture, promise)
    else {
      _run set true
      synchronized(notifyAll())
    }
  }

  @tailrec
  private def remove(done: Iterable[juc.Future[Any]]) {
    val futures = _futures.get
    if (!_futures.compareAndSet(futures, futures -- done))
      remove(done)
  }

  def shutdown() {
    _shutdown set true
    synchronized(notifyAll())
  }

  def apply[T](jfuture: juc.Future[T])(implicit executor: ExecutionContext): Future[T] = {
    val promise = Promise[T]()
    if (jfuture.isDone)
      promise success jfuture.get()
    else
      add(jfuture.asInstanceOf[juc.Future[Any]], promise.asInstanceOf[Promise[Any]])
    promise.future
  }

  @tailrec
  override final def run() {
    if (!_shutdown.get) {
      if (!_run.get) synchronized(wait())
      val futures = _futures.get
      if (futures.isEmpty) {
        _run set false
        if (_futures.get.nonEmpty) _run set true
      } else {
        val done = futures collect {
          case (jfuture, promise) if jfuture.isDone =>
            try promise success jfuture.get() catch { case ex: juc.ExecutionException => promise failure ex}
            jfuture
        }
        if (done.nonEmpty)
          remove(done)
        else
          Thread sleep _sleepTimeMs
      }
      run()
    } else println("Shutting down FutureSelector")
  }
}
