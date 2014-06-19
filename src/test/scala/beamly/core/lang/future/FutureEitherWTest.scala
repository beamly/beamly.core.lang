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

package beamly.core.lang.future

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.Specification

class FutureEitherWTest extends Specification {
  "FutureEitherW" should {
    "join to create failed future" in {
      val exception = new Exception("expected exception")
      val failedFutureEither: Future[Either[Exception, Int]] = Future successful Left(exception)
      failedFutureEither.join.get must throwAn(exception)
    }

    "join to create successful future" in {
      val failedFutureEither: Future[Either[Exception, Int]] = Future successful Right(3)
      failedFutureEither.join.get === 3
    }
  }
}
