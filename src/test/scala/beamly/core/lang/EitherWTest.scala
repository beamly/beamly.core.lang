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


import org.specs2.mutable._

class EitherWTest extends Specification {

  def gt0(n: Int) = Either cond (n > 0, n, Some("%s is not greater than 0" format n))

  "withFilter" >> {
    "simpleForComprehension" >> {
      "Right" ! ((for (a <- Right("foo") if a == "foo") yield a) must beRight("foo"))
      "Left"  ! ((for (a <- Right("foo") if a == "bar") yield a) must beLeft(None))
    }
    "longForComprehension" >> {
      "Right" ! { (for (a <- gt0(1); b <- gt0(2); c <- gt0(3);             d <- gt0(4) if (d == 4)) yield (a, b, c, d)) must beRight((1, 2, 3, 4)) }
      "Left"  ! { (for (a <- gt0(1); b <- gt0(2); c <- gt0(-3);            d <- gt0(4) if (d == 4)) yield (a, b, c, d)) must beLeft(Some("-3 is not greater than 0")) }
      "Left2" ! { (for (a <- gt0(1); b <- gt0(2); c <- gt0(3) if (c == 4); d <- gt0(4)            ) yield (a, b, c, d)) must beLeft(None) }
      "Left3" ! { (for (a <- gt0(1); b <- gt0(2); c <- gt0(3) if (c == 3); d <- gt0(-4)           ) yield (a, b, c, d)) must beLeft(Some("-4 is not greater than 0")) }
    }
    "forComprehensionPatternMatch" >> {
      "Right"  ! { (for (a @ "foo" <- Right("foo")) yield a.length) must beRight(3) }
      "Right2" ! { (for (a @ "foo" <- Right("foo": Any)) yield a.length) must beRight(3) }
      "Left"   ! { (for (a @ "bar" <- Right("foo")) yield a) must beLeft(None) }
    }
  }
}
