package scala.spores.run

import scala.spores._
import Conversions._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class CapturedExcludedSpec {
  @Test
  def `nullary spore allows captured and excluded types`(): Unit = {
    val outsider = "-1"
    val s: NullarySpore[String] {
      type Captured = String
      type Excluded = Double
    } = spore {
      delayed {
        capture(outsider)
      }
    }
    assert(s() == "-1")
  }

  @Test
  def `Spore1 allows captured and excluded types`(): Unit = {
    val outsider = "-1"
    val s: Spore[String, String] {
      type Captured = String
      type Excluded = Double
    } = spore { (v: String) =>
      v + capture(outsider)
    }
    assert(s("1") == "1-1")
  }

  @Test
  def `Spore2 allows captured and excluded types`(): Unit = {
    val outsider = "-1"
    val s: Spore2[String, String, String] {
      type Captured = String
      type Excluded = Double
    } = spore { (v: String, v2: String) =>
      v + v2 + capture(outsider)
    }
    assert(s("1", "2") == "12-1")
  }

}
