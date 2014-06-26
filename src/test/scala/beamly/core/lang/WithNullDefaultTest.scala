package beamly.core.lang

import org.specs2.mutable.Specification
import WithNullDefault._

class WithNullDefaultTest extends Specification {

  "null handling" should {
    "replace with default if null" in {
      val result = (null: String) withNullDefault "bar"
      result must_== "bar"
    }

    "don't replace with default if not null" in {
      val result = "foo" withNullDefault "bar"
      result must_== "foo"
    }

  }

}
