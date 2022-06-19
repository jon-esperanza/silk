package jesperan.silk

import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class SmokeTest extends AnyFlatSpecLike with Matchers {

  behavior.of("smoke test")

  it should "be true" in {
    1 shouldEqual(1);
  }
}
