package scala.spores.run.basic

import scala.spores._

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(classOf[JUnit4])
class SporeAritySpec {
  @Test
  def nullarySpore(): Unit = {
    val s: NullarySpore[Int] = spore {
      delayed {
        42
      }
    }
    assert(s() == 42)
  }

  @Test
  def spore1(): Unit = {
    val s = spore { (i: Int) =>
      i + 42
    }
    assert(s(1) == 43)
  }

  @Test
  def spore2(): Unit = {
    val s = spore { (i: Int, i2: Int) =>
      42 + i + i2
    }
    assert(s(1, 1) == 44)
  }

/*  @Test
  def spore3(): Unit = {
    val s = spore { (i: Int, i2: Int, i3: Int) =>
      42 + i + i2 + i3
    }
    assert(s(1, 1, 1) == 45)
  }

  @Test
  def spore4(): Unit = {
    val s = spore { (i: Int, i2: Int, i3: Int, i4: Int) =>
      42 + i + i2 + i3 + i4
    }
    assert(s(1, 1, 1, 1) == 46)
  }

  @Test
  def spore5(): Unit = {
    val s = spore { (i: Int, i2: Int, i3: Int, i4: Int, i5: Int) =>
      42 + i + i2 + i3 + i4 + i5
    }
    assert(s(1, 1, 1, 1, 1) == 47)
  }

  @Test
  def spore6(): Unit = {
    val s = spore { (i: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int) =>
      42 + i + i2 + i3 + i4 + i5 + i6
    }
    assert(s(1, 1, 1, 1, 1, 1) == 48)
  }

  @Test
  def spore7(): Unit = {
    val s = spore {
      (i: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int) =>
        42 + i + i2 + i3 + i4 + i5 + i6 + i7
    }
    assert(s(1, 1, 1, 1, 1, 1, 1) == 49)
  }*/
}
