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


import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.Specification
import scala.util.{Failure, Success}

class FutureWTest extends Specification {
  "FutureW" should {
    "mapTry for a Success" in {
      Future.successful(3).mapTry {
        case Success(result) => result
        case Failure(exception) => 0
      }.get === 3
    }
    "mapTry for a Failure" in {
      Future.failed(new Exception("exception")).mapTry {
        case Success(result) => result
        case Failure(exception) => 0
      }.get === 0
    }
    
    "flatMapTry for a Success" in {
      Future.successful(3).flatMapTry {
        case Success(result) => Future.successful(result)
        case Failure(exception) => Future.failed(exception)
      }.get === 3
    }
    "flatMapTry for a Failure" in {
      val exception: Exception = new Exception("exception")
      Future.failed[Int](exception).flatMapTry {
        case Success(result) => Future.successful(result)
        case Failure(ex) => Future.failed(ex)
      }.get must throwAn(exception)
    }
    
    "fold for a Success" in {
      Future.successful("hello").fold(
        failed = _.getMessage,
        successful = _ + " world"
      ).get === "hello world"
    }
    "fold for a Failure" in {
      val exception: Exception = new Exception("expected exception")
      Future.failed[String](exception).fold(
        failed = _.getMessage,
        successful = _ + " world"
      ).get === "expected exception"
    }

    "flatFold for a Success" in {
      Future.successful("hello").flatFold(
        failed = Future.failed,
        successful = result => Future.successful(result + " world")
      ).get === "hello world"
    }
    "flatFold for a Failure" in {
      val exception: Exception = new Exception("expected exception")
      Future.failed[String](exception).flatFold(
        failed = Future.failed,
        successful = result => Future.successful(result + " world")
      ).get must throwAn(exception)
    }
  }
}
