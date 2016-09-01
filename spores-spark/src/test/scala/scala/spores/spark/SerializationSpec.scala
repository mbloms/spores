package scala.spores.spark

import scala.spores._
import Conversions._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class SerializationSpec {

  @Test
  def `a captured String is serializable`(): Unit = {
    val greeting = "Hello, World!"
    spore {
      val captured = greeting
      () => println(s"$captured")
    }
  }
}
