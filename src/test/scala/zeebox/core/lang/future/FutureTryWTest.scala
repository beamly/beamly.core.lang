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


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import org.specs2.mutable.Specification

class FutureTryWTest extends Specification {
  "FutureTryW" should {
    "join on success" in {
      Future(Success(3)).join.get === 3
    }
    "join on failure" in {
      val exception = new Exception("Expected exception")
      val failure: Try[Int] = Failure(exception)
      Future(failure).join.get must throwAn(exception)
    }
  }
}
