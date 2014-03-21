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

import org.specs2.mutable.Specification

class OptionWTest extends Specification {
  "Option wrapper" should {
    "on some for some result" in {
      var message: Option[String] = None
      Some(123).onSome { i => message = Some(s"received $i") } === Some(123)
      message must beSome("received 123")
    }
    "on some for none result" in {
      var message: Option[String] = None
      None.onSome { i => message = Some(s"received $i") } === None
      message must beNone
    }
    "on none for none result" in {
      var message: Option[String] = None
      None.onNone { message = Some(s"received nothing") } === None
      message must beSome("received nothing")
    }
    "on none for some result" in {
      var message: Option[String] = None
      Some(123).onNone { message = Some(s"received nothing") } === Some(123)
      message must beNone
    }
  }
}
