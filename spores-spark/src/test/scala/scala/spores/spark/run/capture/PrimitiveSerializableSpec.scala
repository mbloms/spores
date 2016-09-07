package scala.spores.spark.run.capture

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.spores._

@RunWith(classOf[JUnit4])
class PrimitiveSerializableSpec {
  @Test
  def `A captured String is serializable`(): Unit = {
    val greeting = "Hello, World!"
    val s = spore {
      val captured = greeting
      () =>
        s"$captured"
    }
    assert(s() == greeting)
  }

  @Test
  def `A captured Int is serializable`(): Unit = {
    val number: Int = 1
    val s = spore {
      val captured = number
      () =>
        s"$captured"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Byte is serializable`(): Unit = {
    val number: Byte = 1
    val s = spore {
      val captured = number
      () =>
        s"$captured"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Char is serializable`(): Unit = {
    val char: Char = 1
    val s = spore {
      val captured = char
      () =>
        s"$captured"
    }
    assert(s() == char.toString)
  }

  @Test
  def `A captured Short is serializable`(): Unit = {
    val number: Short = 1
    val s = spore {
      val captured = number
      () =>
        s"$captured"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Long is serializable`(): Unit = {
    val number: Long = 1
    val s = spore {
      val captured = number
      () =>
        s"$captured"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Float is serializable`(): Unit = {
    val number: Float = 1
    val s = spore {
      val captured = number
      () =>
        s"$captured"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Double is serializable`(): Unit = {
    val number: Double = 1.0
    val s = spore {
      val captured = number
      () =>
        s"$captured"
    }
    assert(s() == number.toString)
  }
}
