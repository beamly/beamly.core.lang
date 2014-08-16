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
import org.specs2.mutable.Specification

class FutureOptionWTest extends Specification {
  "future option" should {
    "map some values" in {
      Future(Some(2)).mapOption(_ + 1).get must beSome(3)
    }

    "map none values" in {
      val opt: Option[Int] = None
      Future(opt).mapOption(_ + 1).get must beNone
    }

    "flatMap some values" in {
      Future(Some(2)).flatMapOption(i => Future(Some(i + 1))).get must beSome(3)
    }

    "flatMap none values" in {
      val opt: Option[Int] = None
      Future(opt).flatMapOption(i => Future(Some(i + 1))).get must beNone
    }

    "return the result from the first future if this future returns something" in {
      val first = Future successful Some(1)
      val second = Future successful Some(2)
      (first orElse second).get() must beSome(1)
    }

    "return the result from another future if this future returns None" in {
      val first = Future successful None
      val second = Future successful Some(2)
      (first orElse second).get() must beSome(2)
    }

    "return None from another future if this future returns None" in {
      val first = Future successful None
      val second = Future successful None
      (first orElse second).get() must beNone
    }
  }
}
