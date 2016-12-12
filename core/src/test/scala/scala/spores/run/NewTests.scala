package scala.spores.run.newtests

import scala.spores._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


class Box {
  def open(fun: Spore[Int, Unit]): Unit = {
    fun(5)
  }
}

class NonSneaky {
  def process(a: Array[Int]): Unit = {
    for (i <- 0 until a.length)
      a(i) = a(i) + 1
  }
}

@RunWith(classOf[JUnit4])
class NewTestsSpec {

  @Test def createHarmlessInstance(): Unit = {
    val b = new Box
    b.open(spore { (x: Int) =>
      // OK: create instance of harmless class
      val ns = new NonSneaky
    })
  }

}
