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


import java.lang.ref.SoftReference
import org.specs2.mutable.Specification

class RefTest extends Specification {

  "Ref wrapper" should {
    "Create reference" in {
      val value = "Test String"
      val ref = Ref[SoftReference] of value

      Ref get ref must beSome(value)
      Ref getOrElse (ref, 42) must_== value

      Ref clear ref

      Ref get ref must beNone
      Ref getOrElse (ref, 42) must_== 42
    }
  }

}
