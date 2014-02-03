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


import org.specs2.mutable._

class StringWTest extends Specification {
  "isBlank" should {
    def checkString(input: String, expectBlank: Boolean) = {
      input.isBlank === expectBlank
      input.nonBlank !== expectBlank
    }
    "return true when empty" in { checkString("", expectBlank = true) }
    "return true when null" in { checkString(null, expectBlank = true) }
    "return true when only contains whitespace" in { checkString("   \r\n\t  ", expectBlank = true) }
    "return false when contains at least 1 non whitespace character" in { checkString(" t  \r\n\t  ", expectBlank = false) }
  }

  "snake case" should {
    "convert a lowercase string to snake case" in { "name".toSnakeCase === "name" }
    "convert an uppercase string to snake case" in { "NAME".toSnakeCase === "name" }
    "convert a mixed case string to snake case" in { "EpisodeId".toSnakeCase === "episode_id" }
    "convert a mixed case string to snake case" in { "zeeBOXstuff".toSnakeCase === "zee_box_stuff" }
    "convert a mixed case string to snake case" in { "ZEEboxSTUFF".toSnakeCase === "zee_box_stuff" }
  }
}
