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

import scala.concurrent.duration._
import java.util.{concurrent => juc}
import scala.concurrent.{ExecutionContext, Promise, Future}
import annotation.tailrec

/**
 * A helper that polls for the completion of Java Futures. The results of the Java Futures
 * are returned through Akka Futures.
 *
 * @param sleepTime The duration that the selector will wait between each iteration
 */
class FutureSelector(sleepTime: Duration = Duration(1, MILLISECONDS)) {
  @volatile private[this] var _futures = Map.empty[juc.Future[Any], Promise[Any]]
  @volatile private[this] var _shutdown = false
  private[this] val sleepTimeMs = sleepTime.toMillis
  private[this] val lock = new juc.locks.ReentrantLock()
  private[this] val notEmpty = lock.newCondition()

  private[this] val thread = new Thread {
    @tailrec
    override final def run() {
      if (!_shutdown) {
        val futures = _futures
        if (futures.isEmpty) {
          lock.lock()
          try {
            if (_futures.isEmpty)
              notEmpty.await()
          } finally {
            lock.unlock()
          }
        } else {
          val done = futures collect {
            case (jfuture, promise) if jfuture.isDone =>
              try promise success jfuture.get() catch {
                case ex: juc.ExecutionException => promise failure ex
              }
              jfuture
          }
          if (done.nonEmpty)
            remove(done)
          else
            Thread sleep sleepTimeMs
        }
        run()
      }
    }
  }

  private def add(jfuture: juc.Future[Any], promise: Promise[Any]) {
    lock.lock()
    try {
      val futures = _futures
      _futures = futures + (jfuture -> promise)
      if (futures.isEmpty)
        notEmpty.signalAll()
    } finally {
      lock.unlock()
    }
  }

  private def remove(done: Iterable[juc.Future[Any]]) {
    lock.lock()
    try {
      _futures --= done
    } finally {
      lock.unlock()
    }
  }

  def start() {
    thread.start()
  }

  def shutdown() {
    _shutdown = true
    notEmpty.signalAll()
  }

  def apply[T](jfuture: juc.Future[T])(implicit executor: ExecutionContext): Future[T] = {
    val promise = Promise[T]()
    if (jfuture.isDone)
      promise success jfuture.get()
    else
      add(jfuture.asInstanceOf[juc.Future[Any]], promise.asInstanceOf[Promise[Any]])
    promise.future
  }

}
