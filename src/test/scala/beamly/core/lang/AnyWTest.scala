package beamly.core.lang

import org.specs2.mutable.Specification

class AnyWTest extends Specification {
  "matchOption" >> {
    "returns Some on match"    >> { ("abc" matchOption { case "abc" => "yes" }) ==== Some("yes") }
    "returns None on no match" >> { ("def" matchOption { case "abc" => "yes" }) ==== None }
  }
}
