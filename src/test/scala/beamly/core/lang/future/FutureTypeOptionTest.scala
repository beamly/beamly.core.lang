package beamly.core.lang.future

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.specs2.mutable.Specification

class FutureTypeOptionTest extends Specification {
  "Future type" should {
    "convert None to Future of None" in {
      Future.option(None)(_ => Future.successful(3)).get === None
    }

    "convert Some value to Future of that value" in {
      Future.option(Some(3))(i => Future.successful(s"max value was $i")).get === Some("max value was 3")
    }
  }
}
