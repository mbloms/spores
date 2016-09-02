package scala.spores.spark

import scala.spores._
import Conversions._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class SerializationSpec {
  @Test
  def `A captured String is serializable`(): Unit = {
    val greeting = "Hello, World!"
    spore {
      val captured = greeting
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Int is serializable`(): Unit = {
    val number: Int = 1
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Byte is serializable`(): Unit = {
    val number: Byte = 1
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Char is serializable`(): Unit = {
    val number: Char = 1
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Short is serializable`(): Unit = {
    val number: Short = 1
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Long is serializable`(): Unit = {
    val number: Long = 1
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Float is serializable`(): Unit = {
    val number: Float = 1
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }

  @Test
  def `A captured Double is serializable`(): Unit = {
    val number: Double = 1.0
    spore {
      val captured = number
      () => println(s"$captured")
    }
  }
}
