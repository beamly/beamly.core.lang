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
    "convert a mixed case string to snake case" in { "beamLYstuff".toSnakeCase === "beam_ly_stuff" }
    "convert a mixed case string to snake case" in { "BEAMlySTUFF".toSnakeCase === "beam_ly_stuff" }
  }

  "toBooleanOption" should {
    "convert true"  in {  "true".toBooleanOption ==== Some(true) }
    "convert false" in { "false".toBooleanOption ==== Some(false) }
    "not convert a" in {     "a".toBooleanOption ==== None }
  }
  "toByteOption" should {
    "convert MinValue" in {  Byte.MinValue.toString.toByteOption ==== Some(Byte.MinValue) }
    "convert 0"        in {                     "0".toByteOption ==== Some(0.toByte) }
    "convert MaxValue" in {  Byte.MaxValue.toString.toByteOption ==== Some(Byte.MaxValue) }
    "not convert a"    in {                     "a".toByteOption ==== None }
  }
  "toShortOption" should {
    "convert MinValue" in {  Short.MinValue.toString.toShortOption ==== Some(Short.MinValue) }
    "convert 0"        in {                      "0".toShortOption ==== Some(0.toShort) }
    "convert MaxValue" in {  Short.MaxValue.toString.toShortOption ==== Some(Short.MaxValue) }
    "not convert a"    in {                      "a".toShortOption ==== None }
  }
  "toIntOption" should {
    "convert MinValue" in {  Int.MinValue.toString.toIntOption ==== Some(Int.MinValue) }
    "convert 0"        in {                    "0".toIntOption ==== Some(0) }
    "convert MaxValue" in {  Int.MaxValue.toString.toIntOption ==== Some(Int.MaxValue) }
    "not convert a"    in {                    "a".toIntOption ==== None }
  }
  "toLongOption" should {
    "convert MinValue" in {  Long.MinValue.toString.toLongOption ==== Some(Long.MinValue) }
    "convert 0"        in {                     "0".toLongOption ==== Some(0L) }
    "convert MaxValue" in {  Long.MaxValue.toString.toLongOption ==== Some(Long.MaxValue) }
    "not convert a"    in {                     "a".toLongOption ==== None }
  }
  "toFloatOption" should {
    "convert MinValue" in {  Float.MinValue.toString.toFloatOption ==== Some(Float.MinValue) }
    "convert 0"        in {                      "0".toFloatOption ==== Some(0.0f) }
    "convert MaxValue" in {  Float.MaxValue.toString.toFloatOption ==== Some(Float.MaxValue) }
    "not convert a"    in {                      "a".toFloatOption ==== None }
  }
  "toDoubleOption" should {
    "convert MinValue" in {  Double.MinValue.toString.toDoubleOption ==== Some(Double.MinValue) }
    "convert 0"        in {                       "0".toDoubleOption ==== Some(0.0) }
    "convert MaxValue" in {  Double.MaxValue.toString.toDoubleOption ==== Some(Double.MaxValue) }
    "not convert a"    in {                       "a".toDoubleOption ==== None }
  }
}
