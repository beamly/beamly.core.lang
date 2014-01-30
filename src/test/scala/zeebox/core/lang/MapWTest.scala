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

package zeebox.core.lang

import org.specs2.mutable.Specification

class MapWTest extends Specification {
  "MapW" should {
    "merge the values of 2 maps together" in {
      val left = Map(1 -> 100, 2 -> 222)
      val right = Map(3 -> 33, 1 -> 99)
      left.mergeValues(right) { case (leftValues,rightValues) =>
        leftValues.getOrElse(0) + rightValues.getOrElse(0)
      } === Map(1 -> 199, 2 -> 222, 3 -> 33)
    }
  }
}
