package scala.spores.spark.run.capture

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

  @Test
  def `An object extending Serializable is serializable`(): Unit = {
    object A extends Serializable { val s = 1 }
    val a = A
    val s = spore {
      () => capture(a)
    }
    assert(s() == a)
  }

  @Test
  def `An abstract class extending Serializable is serializable`(): Unit = {
    abstract class A extends Serializable { val i: Int }
    val a = new A { val i = 1 }
    val s = spore {
      () => capture(a)
    }
    assert(s() == a)
  }

  @Test
  def `A class extending Serializable is serializable`(): Unit = {
    class A(val i: Int) extends Serializable
    val a = new A(1)
    val s = spore {
      () => capture(a)
    }
    assert(s() == a)
  }
}
