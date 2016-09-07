package scala.spores.spark.run

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class SerializableSpec {
  @Test
  def `A case class is serializable`(): Unit = {
    case class A(number: Int)
    val a = A(1)
    val s = spore {
      () => capture(a)
    }
    assert(s() == a)
  }

  @Test
  def `A case object is serializable`(): Unit = {
    case object A { val s = 1 }
    val a = A
    val s = spore {
      () => capture(a)
    }
    assert(s() == a)
  }
}
