package beamly.core.lang

import org.specs2.mutable._

class TraversableWithMaxOptionTest extends Specification {
  "maxOption" >> {
    "empty sequence" >> {
      Seq[Int]().maxOption ==== None
    }
    "int sequences" >> {
      Seq(1, 2, 3).maxOption ==== Some(3)
      Seq(3, 2, 1).maxOption ==== Some(3)
    }
  }
}
