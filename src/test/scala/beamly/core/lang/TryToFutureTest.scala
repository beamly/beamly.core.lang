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

package beamly.core.lang


import scala.concurrent.Future
import scala.util.{Success, Failure}
import beamly.core.lang.TryToFuture._
import beamly.core.lang.future._
import org.specs2.mutable.Specification

class TryToFutureTest extends Specification {
  "TryToFuture" should {
    "convert failed Try to failed Future" in {
      val exception = new Exception("expected error")
      val failedTry: Failure[Int] = Failure(exception)
      val failedFuture: Future[Int] = failedTry
      failedFuture.get must throwAn(exception)
    }

    "convert successful Try to successful Future" in {
      val successfulFuture: Future[Int] = Success(3)
      successfulFuture.get === 3
    }
  }
}
