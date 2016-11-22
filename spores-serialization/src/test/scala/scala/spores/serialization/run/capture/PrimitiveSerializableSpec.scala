package scala.spores.serialization.run.capture

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
      () => s"${capture(greeting)}"
    }
    assert(s() == greeting)
  }

  @Test
  def `A captured Int is serializable`(): Unit = {
    val number: Int = 1
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Byte is serializable`(): Unit = {
    val number: Byte = 1
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Char is serializable`(): Unit = {
    val char: Char = 1
    val s = spore {
      () => s"${capture(char)}"
    }
    assert(s() == char.toString)
  }

  @Test
  def `A captured Short is serializable`(): Unit = {
    val number: Short = 1
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Long is serializable`(): Unit = {
    val number: Long = 1
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Float is serializable`(): Unit = {
    val number: Float = 1
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured Double is serializable`(): Unit = {
    val number: Double = 1.0
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured list of String is serializable`(): Unit = {
    val greeting = List("Hello, World!")
    val s = spore {
      () => s"${capture(greeting)}"
    }
    assert(s() == greeting.toString)
  }

  @Test
  def `A captured list of Int is serializable`(): Unit = {
    val number = List(1)
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured list of Byte is serializable`(): Unit = {
    val number = List(1.toByte)
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured list of Char is serializable`(): Unit = {
    val char = List(1.toChar)
    val s = spore {
      () => s"${capture(char)}"
    }
    assert(s() == char.toString)
  }

  @Test
  def `A captured list of Short is serializable`(): Unit = {
    val number = List(1.toShort)
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured list of Long is serializable`(): Unit = {
    val number = List(1.toLong)
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured list of Float is serializable`(): Unit = {
    val number = List(1.toFloat)
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }

  @Test
  def `A captured list of Double is serializable`(): Unit = {
    val number = List(1.0)
    val s = spore {
      () => s"${capture(number)}"
    }
    assert(s() == number.toString)
  }
}
