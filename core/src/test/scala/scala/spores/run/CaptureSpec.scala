package scala.spores.run

import scala.spores._
import ExcludedSporeConversions._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class CaptureSpec {
  @Test
  def `detect captured variables with capture`(): Unit = {
    val helloWorld = "Hello World"
    val s: NullarySpore[String] {type Captured = String} = spore {
      () => capture(helloWorld)
    }
    assert(s() == helloWorld)
  }
}
